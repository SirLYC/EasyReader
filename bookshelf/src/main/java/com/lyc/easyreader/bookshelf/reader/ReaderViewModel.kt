package com.lyc.easyreader.bookshelf.reader

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.api.book.*
import com.lyc.easyreader.base.arch.LiveState
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.arch.SingleLiveEvent
import com.lyc.easyreader.base.ui.ReaderHeadsUp
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.base.utils.thread.CancelToken
import com.lyc.easyreader.base.utils.thread.CancelTokenList
import com.lyc.easyreader.bookshelf.BookManager
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.measureTimeMillis

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
class ReaderViewModel : ViewModel(), IBookManager.IBookChangeListener,
    BookShelfOpenHelper.IBookMarkChangeListener {
    companion object {
        const val TAG = "ReaderViewModel"
        const val KEY_BOOK_FILE = "${TAG}_KEY_BOOK_FILE"
        const val KEY_SECRET_MODE = "${TAG}_SECRET_MODE"
    }

    private val handler = Handler(Looper.getMainLooper())

    init {
        BookShelfOpenHelper.instance.addBookMarkChangeListener(this)
        BookManager.instance.addBookChangeListener(this)
    }

    @Volatile
    var alive = true

    private var secretMode = false
    val bookFileLiveData = MutableLiveData<BookFile>()
    val bookChapterList = ObservableList<BookChapter>(arrayListOf())
    val loadingChapterListLiveData = NonNullLiveData(true)
    val showMenu = LiveState(false)
    val pageCount = NonNullLiveData(0)
    val currentPage = NonNullLiveData(0)
    val currentChapter = NonNullLiveData(0)
    val chapterReverse = NonNullLiveData(false)
    val charOffsets = intArrayOf(-1, -1)
    private val cancelTokenList = CancelTokenList()
    var parseResult: BookParser.ParseChapterResult? = null
    private var isLoadingBookMarks = false
    val bookMarkList = ObservableList<BookMark>(arrayListOf())

    var collected = false
    var bookReadRecord: BookReadRecord? = null

    val changeChapterCall = SingleLiveEvent<Int>()
    val skipBookMarkCall = SingleLiveEvent<Triple<Int, Int, Int>>()

    fun init(bookFile: BookFile, secretMode: Boolean) {
        bookFileLiveData.value = bookFile
        this.secretMode = secretMode
        if (secretMode) {
            ReaderHeadsUp.showHeadsUp("私密模式下不会记录阅读记录")
        }
        loadChapterIfNeeded()
    }

    private fun loadChapterIfNeeded() {
        if (bookChapterList.isNotEmpty()) {
            return
        }
        loadingChapterListLiveData.value = true
        bookFileLiveData.value?.let { bookFile ->
            ExecutorFactory.CPU_BOUND_EXECUTOR.execute {
                val newBookFile = BookShelfOpenHelper.instance.reloadBookFile(bookFile) ?: bookFile
                var chapterList = BookShelfOpenHelper.instance.queryBookChapters(newBookFile)
                if (chapterList == null || chapterList.isEmpty()) {
                    val parser = BookParser(newBookFile)

                    val resultCapture = AtomicReference<BookParser.ParseChapterResult>()
                    val cancelToken = CancelToken()
                    val time = measureTimeMillis {
                        resultCapture.set(parser.parseChapters(cancelToken.also {
                            cancelTokenList.add(
                                it
                            )
                        }))
                    }
                    LogUtils.d(TAG, "Parse book time: ${time}ms")
                    val result: BookParser.ParseChapterResult = resultCapture.get()
                    handler.post { parseResult = result }

                    if (result.code == BookParser.CODE_CANCEL || cancelToken.canceld) {
                        LogUtils.i(TAG, "Parse canceled.")
                        return@execute
                    }

                    if (result.code != BookParser.CODE_SUCCESS) {
                        ReaderToast.showToast(result.msg)
                        LogUtils.e(
                            TAG,
                            "Parse book failed! Result=${result.msg}",
                            ex = result.exception
                        )
                        handler.post {
                            if (!alive) {
                                return@post
                            }
                            loadingChapterListLiveData.value = false
                        }
                        return@execute
                    }
                    chapterList = result.list
                }
                val nonnullList = chapterList!!
                val bookCollect = BookShelfOpenHelper.instance.queryBookCollect(bookFile)
                val bookRecord = BookShelfOpenHelper.instance.loadBookRecordOrNew(bookFile)
                handler.post {
                    if (!alive) {
                        return@post
                    }
                    bookCollect?.let {
                        this.collected = it.collected
                    }
                    bookFileLiveData.value = newBookFile
                    if (secretMode) {
                        LogUtils.i(TAG, "私密模式跳过记录AccessTime!")
                    } else {
                        BookShelfOpenHelper.instance.asyncSaveUpdateBookAccess(newBookFile)
                    }
                    currentPage.value = bookRecord.page
                    currentChapter.value = bookRecord.chapter
                    charOffsets[0] = bookRecord.offsetStart
                    charOffsets[0] = bookRecord.offsetEnd
                    nonnullList.forEach {
                        if (it.chapterType == BookChapter.ChapterType.SINGLE) {
                            it.title = bookFile.filename
                        }
                    }
                    bookChapterList.addAll(nonnullList)
                    loadingChapterListLiveData.value = false
                }
            }
        }
    }

    fun loadBookMarksIfNeeded() {
        if (bookMarkList.isNotEmpty()) {
            return
        }
        refreshBookMarks()
    }

    private fun refreshBookMarks() {
        if (isLoadingBookMarks) {
            return
        }

        isLoadingBookMarks = true
        bookFileLiveData.value?.let { bookFile ->
            ExecutorFactory.IO_EXECUTOR.execute {
                val bookMarks = BookShelfOpenHelper.instance.loadBookMarks(bookFile.id)
                handler.post {
                    bookMarkList.replaceAll(bookMarks)
                    isLoadingBookMarks = false
                }
            }
        }
    }

    fun updateCollectState(collect: Boolean) {
        bookFileLiveData.value?.run {
            BookManager.instance.updateBookCollect(id, collect)
        }
    }

    fun updateBookReadRecord(chapter: Int, page: Int) {
        if (chapter < 0 || page < 0 || charOffsets[0] == -1 || charOffsets[0] == -1 || charOffsets[1] < charOffsets[0]) {
            return
        }

        if (chapter >= bookChapterList.size) {
            return
        }

        val desc = bookChapterList[chapter].title
        bookFileLiveData.value?.let { bookFile ->
            val record = bookReadRecord?.apply {
                this.chapter = chapter
                this.page = page
                this.offsetStart = charOffsets[0]
                this.offsetEnd = charOffsets[1]
                this.desc = desc
            } ?: BookReadRecord(
                bookFile.id,
                chapter,
                charOffsets[0],
                charOffsets[1],
                page,
                desc
            ).also { this.bookReadRecord = it }
            if (secretMode) {
                LogUtils.i(TAG, "私密模式，跳过记录进度到数据库！")
            } else {
                BookShelfOpenHelper.instance.asyncUpdateBookReadRecord(record)
            }
        }
    }

    fun saveState(bundle: Bundle) {
        bundle.putParcelable(KEY_BOOK_FILE, bookFileLiveData.value)
        bundle.putBoolean(KEY_SECRET_MODE, secretMode)
    }

    fun restoreState(bundle: Bundle) {
        bookFileLiveData.value = bundle.getParcelable(KEY_BOOK_FILE)
        secretMode = bundle.getBoolean(KEY_SECRET_MODE, false)
        loadChapterIfNeeded()
    }

    override fun onCleared() {
        alive = false
        cancelTokenList.clear()
        handler.removeCallbacksAndMessages(null)
        BookShelfOpenHelper.instance.removeBookMarkChangeListener(this)
        BookManager.instance.removeBookChangeListener(this)
        super.onCleared()
    }

    override fun onBooksImported(list: List<BookFile>) {}

    override fun onBookDeleted() {}

    override fun onBookCollectChange(id: String, collect: Boolean) {
        handler.post {
            if (id == bookFileLiveData.value?.id) {
                this.collected = collect
            }
        }
    }

    override fun onBookInfoUpdate(id: String) {

    }

    override fun onBookMarkChange(bookId: String) {
        handler.post {
            refreshBookMarks()
        }
    }
}
