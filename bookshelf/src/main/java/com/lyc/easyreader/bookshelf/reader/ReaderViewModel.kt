package com.lyc.easyreader.bookshelf.reader

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.arch.LiveState
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.arch.SingleLiveEvent
import com.lyc.easyreader.base.ui.ReaderToast
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

    var bookFile: BookFile?
        get() = bookFileLiveData.value
        private set(value) {
            bookFileLiveData.value = value
        }
    val bookFileLiveData = MutableLiveData<BookFile>()
    val bookChapterList = ObservableList<BookChapter>(arrayListOf())
    val loadingChapterListLiveData = NonNullLiveData(true)
    val showMenu = LiveState(false)
    val pageCount = NonNullLiveData(0)
    val currentPage = NonNullLiveData(0)
    val currentChapter = NonNullLiveData(0)
    val chapterReverse = NonNullLiveData(false)

    val changeChapterCall = SingleLiveEvent<Int>()

    fun init(bookFile: BookFile) {
        this.bookFile = bookFile
        loadChapterIfNeeded()
    }

    private fun loadChapterIfNeeded() {
        if (bookChapterList.isNotEmpty()) {
            return
        }
        loadingChapterListLiveData.value = true
        bookFile?.let { bookFile ->
            ExecutorFactory.CPU_BOUND_EXECUTOR.execute {
                val newBookFile = BookShelfOpenHelper.instance.reloadBookFile(bookFile) ?: bookFile
                var chapterList = BookShelfOpenHelper.instance.queryBookChapters(newBookFile)
                if (chapterList == null || chapterList.isEmpty()) {
                    val parser = BookParser(bookFile)
                    val result = parser.parseChapters()
                    if (result.code != BookParser.CODE_SUCCESS) {
                        ReaderToast.showToast(result.msg)
                        return@execute
                    }
                    chapterList = result.list
                }
                handler.post {
                    if (!alive) {
                        return@post
                    }
                    this.bookFile = newBookFile
                    BookShelfOpenHelper.instance.asyncSaveUpdateBookAccess(newBookFile)
                    currentPage.value = newBookFile.lastPageInChapter
                    currentChapter.value = newBookFile.lastChapter
                    bookChapterList.addAll(chapterList!!)
                    loadingChapterListLiveData.value = false
                }
            }
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
        bookFile?.let {
            BookShelfOpenHelper.instance.asyncSaveUpdateBookRecord(it, chapter, page, desc)
        }
    }

    fun saveState(bundle: Bundle) {
        bundle.putParcelable(KEY_BOOK_FILE, bookFile)
    }

    fun restoreState(bundle: Bundle) {
        bookFile = bundle.getParcelable(KEY_BOOK_FILE)
        loadChapterIfNeeded()
    }

    override fun onCleared() {
        alive = false
        handler.removeCallbacksAndMessages(null)
        super.onCleared()
    }
}
