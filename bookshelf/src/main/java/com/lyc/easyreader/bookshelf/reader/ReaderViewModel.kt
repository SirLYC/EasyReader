package com.lyc.easyreader.bookshelf.reader

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.api.book.BookCollect
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.arch.LiveState
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.arch.SingleLiveEvent
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
class ReaderViewModel : ViewModel() {
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

    var bookCollect: BookCollect? = null

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
                    if (result.code != BookParser.CODE_SUCCESS) {
                        ReaderToast.showToast(result.msg)
                        LogUtils.e(
                            TAG,
                            "Parse book failed! Result=${result.msg}",
                            ex = result.exception
                        )
                        return@execute
                    }
                    chapterList = result.list
                }
                val nonnullList = chapterList!!
                val bookCollect = BookShelfOpenHelper.instance.queryBookCollect(bookFile)
                handler.post {
                    if (!alive) {
                        return@post
                    }
                    this.bookCollect = bookCollect ?: BookCollect(newBookFile.id, false, 0)
                    bookFileLiveData.value = newBookFile
                    BookShelfOpenHelper.instance.asyncSaveUpdateBookAccess(newBookFile)
                    currentPage.value = newBookFile.lastPageInChapter
                    currentChapter.value = newBookFile.lastChapter
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
            val bookCollect =
                this@ReaderViewModel.bookCollect?.also { it.collected = collect } ?: BookCollect(
                    id,
                    collect,
                    0
                ).also {
                    this@ReaderViewModel.bookCollect = it
                }
            BookShelfOpenHelper.instance.updateBookCollect(bookCollect)
            ReaderToast.showToast(if (collect) "已收藏" else "已取消收藏")
        }
    }

    fun updateRecord(chapter: Int, page: Int) {
        if (chapter < 0 || page < 0) {
            return
        }

        if (chapter >= bookChapterList.size) {
            return
        }

        val desc = bookChapterList[chapter].title
        bookFileLiveData.value?.let {
            BookShelfOpenHelper.instance.asyncSaveUpdateBookRecord(it, chapter, page, desc)
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
}
