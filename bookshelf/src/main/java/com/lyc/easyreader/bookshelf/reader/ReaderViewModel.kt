package com.lyc.easyreader.bookshelf.reader

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.api.book.BookReadRecord
import com.lyc.easyreader.api.book.IBookManager
import com.lyc.easyreader.base.arch.LiveState
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.arch.SingleLiveEvent
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.bookshelf.BookManager
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
class ReaderViewModel : ViewModel(), IBookManager.IBookChangeListener {
    companion object {
        const val TAG = "ReaderViewModel"
        const val KEY_BOOK_FILE = "${TAG}_KEY_BOOK_FILE"
    }

    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    var alive = true

    val bookFileLiveData = MutableLiveData<BookFile>()
    val bookChapterList = ObservableList<BookChapter>(arrayListOf())
    val loadingChapterListLiveData = NonNullLiveData(true)
    val showMenu = LiveState(false)
    val pageCount = NonNullLiveData(0)
    val currentPage = NonNullLiveData(0)
    val currentChapter = NonNullLiveData(0)
    val chapterReverse = NonNullLiveData(false)
    val charOffsets = intArrayOf(-1, -1)
    var parseResult: BookParser.ParseChapterResult? = null

    var collected = false
    var bookReadRecord: BookReadRecord? = null

    val changeChapterCall = SingleLiveEvent<Int>()

    fun init(bookFile: BookFile) {
        bookFileLiveData.value = bookFile
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
                    val result = parser.parseChapters()
                    handler.post { parseResult = result }
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
                    BookShelfOpenHelper.instance.asyncSaveUpdateBookAccess(newBookFile)
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
        bookFileLiveData.value?.let {
            val record = bookReadRecord?.apply {
                this.chapter = chapter
                this.page = page
                this.offsetStart = charOffsets[0]
                this.offsetEnd = charOffsets[1]
                this.desc = desc
            } ?: BookReadRecord(
                it.id,
                chapter,
                charOffsets[0],
                charOffsets[1],
                page,
                desc
            )
            BookShelfOpenHelper.instance.asyncUpdateBookReadRecord(record)
        }
    }

    fun saveState(bundle: Bundle) {
        bundle.putParcelable(KEY_BOOK_FILE, bookFileLiveData.value)
    }

    fun restoreState(bundle: Bundle) {
        bookFileLiveData.value = bundle.getParcelable(KEY_BOOK_FILE)
        loadChapterIfNeeded()
    }

    override fun onCleared() {
        alive = false
        handler.removeCallbacksAndMessages(null)
        super.onCleared()
    }

    override fun onBooksImported(list: List<BookFile>) {}

    override fun onBookDeleted(id: String) {}

    override fun onBookCollectChange(id: String, collect: Boolean) {
        handler.post {
            if (id == bookFileLiveData.value?.id) {
                this.collected = collect
            }
        }
    }

    override fun onBookInfoUpdate(id: String) {

    }
}
