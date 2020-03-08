package com.lyc.easyreader.bookshelf.secret

import android.os.Handler
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.api.book.IBookManager
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.bookshelf.BookManager
import com.lyc.easyreader.bookshelf.db.BookShelfBook
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
class SecretViewModel : ViewModel(), IBookManager.IBookChangeListener,
    BookShelfOpenHelper.IBookFileInfoUpdateListener {
    private val handler = Handler()
    var firstRefreshFinish = false
        private set
    val isRefreshingLiveData = NonNullLiveData(false)
    val secretBookList = ObservableList<BookShelfBook>(arrayListOf())

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
            val list = BookShelfOpenHelper.instance.loadBookShelfBookList(true)
            ExecutorFactory.MAIN_EXECUTOR.execute {
                firstRefreshFinish = true
                secretBookList.replaceAll(list)
                isRefreshingLiveData.value = false
            }
        }
    }

    override fun onBookDeleted() {
        handler.post { refreshList() }
    }

    override fun onSecretBooksChanged() {
        handler.post { refreshList() }
    }

    override fun onBookInfoUpdate(id: String) {
        handler.post { refreshList() }
    }

    override fun onBookInfoUpdate() {
        handler.post { refreshList() }
    }
}
