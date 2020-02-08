package com.lyc.easyreader.bookshelf.reader

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.arch.LiveState
import com.lyc.easyreader.base.arch.NonNullLiveData
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

    var bookFile: BookFile? = null
        private set
    val bookChapterList = ObservableList<BookChapter>(arrayListOf())
    val loadingChapterListLiveData = NonNullLiveData(true)
    val showMenu = LiveState(false)
    val pageCount = NonNullLiveData(0)
    val currentPage = NonNullLiveData(0)
    val currentChapter = NonNullLiveData(0)

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
                var chapterList = BookShelfOpenHelper.instance.queryBookChapters(bookFile)
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
                    bookChapterList.addAll(chapterList!!)
                    loadingChapterListLiveData.value = false
                }
            }
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
