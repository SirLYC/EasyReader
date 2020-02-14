package com.lyc.easyreader.bookshelf.collect

import android.os.Handler
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.api.book.IBookManager
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.bookshelf.BookManager
import com.lyc.easyreader.bookshelf.db.BookShelfBook
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
class CollectViewModel : ViewModel(), IBookManager.IBookChangeListener,
    BookShelfOpenHelper.IBookFileInfoUpdateListener {
    private val handler = Handler()
    var firstRefreshFinish = false
        private set
    val isRefreshingLiveData = NonNullLiveData(false)
    val collectBookList = ObservableList<BookShelfBook>(arrayListOf())

    init {
        BookShelfOpenHelper.instance.addBookFileInfoUpdateListener(this)
        BookManager.instance.addBookChangeListener(this)
    }

    override fun onCleared() {
        BookShelfOpenHelper.instance.removeBookFileInfoUpdateListener(this)
        BookManager.instance.removeBookChangeListener(this)
        handler.removeCallbacksAndMessages(null)
        super.onCleared()
    }

    @MainThread
    fun refreshList() {
        if (isRefreshingLiveData.value) {
            return
        }
        isRefreshingLiveData.value = true
        ExecutorFactory.IO_EXECUTOR.execute {
            val list = BookShelfOpenHelper.instance.loadCollectBookList()
            ExecutorFactory.MAIN_EXECUTOR.execute {
                firstRefreshFinish = true
                collectBookList.replaceAll(list)
                isRefreshingLiveData.value = false
            }
        }
    }

    override fun onBooksImported(list: List<BookFile>) {}

    override fun onBookDeleted() {
        handler.post { refreshList() }
    }

    override fun onBookCollectChange(id: String, collect: Boolean) {
        handler.post { refreshList() }
    }

    override fun onBookInfoUpdate(id: String) {
        handler.post { refreshList() }
    }

    override fun onBookInfoUpdate() {
        handler.post { refreshList() }
    }
}
