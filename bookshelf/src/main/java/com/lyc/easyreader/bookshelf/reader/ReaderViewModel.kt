package com.lyc.easyreader.bookshelf.reader

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.api.book.BookFile
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
        const val KEY_BOOK_CHAPTERS = "${TAG}_KEY_BOOK_CHAPTERS"
    }

    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    var alive = false

    var bookFile: BookFile? = null
        private set
    val bookChapterList = ObservableList<BookChapter>(arrayListOf())
    val loadingChapterListLiveDate = NonNullLiveData(true)

    fun init(bookFile: BookFile) {
        this.bookFile = bookFile
        loadChapterIfNeeded()
    }

    private fun loadChapterIfNeeded() {
        if (bookChapterList.isNotEmpty()) {
            return
        }
        loadingChapterListLiveDate.value = true
        bookFile?.let { bookFile ->
            ExecutorFactory.IO_EXECUTOR.execute {
                var chapterList = BookShelfOpenHelper.instance.queryBookChapters(bookFile)
                if (chapterList == null || chapterList.isEmpty()) {
                    val parser = BookParser(bookFile)
                    val result = parser.parseChapters()
                    if (result.code != BookParser.CODE_SUCCESS) {
                        ReaderToast.showToast(result.msg)
                        return@execute
                    }
                    chapterList = BookShelfOpenHelper.instance.queryBookChapters(bookFile)
                }
                handler.post {
                    bookChapterList.addAll(chapterList!!)
                    loadingChapterListLiveDate.value = false
                }
            }
        }
    }

    fun saveState(bundle: Bundle) {
        bundle.putParcelable(KEY_BOOK_FILE, bookFile)
        bundle.putParcelableArrayList(KEY_BOOK_CHAPTERS, ArrayList(bookChapterList))
    }

    fun restoreState(bundle: Bundle) {
        bookFile = bundle.getParcelable(KEY_BOOK_FILE)
        bundle.getParcelableArrayList<BookChapter>(KEY_BOOK_CHAPTERS)?.let {
            bookChapterList.addAll(it)
        }
        loadChapterIfNeeded()
    }

    override fun onCleared() {
        alive = false
        super.onCleared()
    }
}
