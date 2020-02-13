package com.lyc.easyreader.bookshelf

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.api.book.IBookManager
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.ui.ReaderHeadsUp
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.bookshelf.db.BookShelfBook
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class BookShelfViewModel : ViewModel(), IBookManager.IBookChangeListener,
    BookShelfOpenHelper.IBookFileInfoUpdateListener {
    private val handler = Handler(Looper.getMainLooper())

    val hasDataLiveData = MutableLiveData<Boolean>()
    val isLoadingLiveData = NonNullLiveData(false)
    val list = ObservableList(arrayListOf<BookShelfBook>())
    var firstLoadFinish = false
        private set
    val editModeLiveData = NonNullLiveData(false)
    val checkedIds = arrayListOf<String>()
    var afterDataUpdate: (() -> Unit)? = null

    init {
        BookManager.instance.addBookChangeListener(this)
        BookShelfOpenHelper.instance.addBookFileInfoUpdateListener(this)
    }

    fun firstLoadIfNeeded() {
        if (firstLoadFinish || isLoadingLiveData.value) {
            return
        }
        refreshList()
    }

    fun getCheckItems(): List<BookShelfBook> {
        val set = checkedIds.toSet()
        return list.filter { set.contains(it.id) }
    }

    @MainThread
    fun refreshList(fromCallback: Boolean = false) {
        if (isLoadingLiveData.value) {
            return
        }
        isLoadingLiveData.value = true
        val currentList = list.toList()
        ExecutorFactory.IO_EXECUTOR.execute {
            BookShelfOpenHelper.instance.loadBookShelfBookList()
            val shelfBooks = BookShelfOpenHelper.instance.loadBookShelfBookList()
            val diffResultRef = AtomicReference<DiffUtil.DiffResult>(null)
            val hasChange = AtomicBoolean(true)
            val newBookIds = shelfBooks.map { it.id }.toSet()
            val mainTask = Runnable {
                val diffResult: DiffUtil.DiffResult? = diffResultRef.get()
                checkedIds.iterator().let {
                    while (it.hasNext()) {
                        val id = it.next()
                        if (!newBookIds.contains(id)) {
                            it.remove()
                        }
                    }
                }
                if (diffResult == null) {
                    if (hasChange.get()) {
                        list.replaceAll(shelfBooks)
                        if (firstLoadFinish && !fromCallback) {
                            ReaderHeadsUp.showHeadsUp("刷新完成")
                        }
                    } else if (firstLoadFinish && !fromCallback) {
                        ReaderHeadsUp.showHeadsUp("没有更新")
                    }
                } else {
                    list.withoutCallback {
                        list.clear()
                        list.addAll(shelfBooks)
                    }
                    diffResult.dispatchUpdatesTo(list)
                    if (firstLoadFinish && !fromCallback) {
                        ReaderHeadsUp.showHeadsUp("刷新完成")
                    }
                }
                firstLoadFinish = true
                isLoadingLiveData.value = false
                checkUpdateHasData()
                afterDataUpdate?.invoke()
            }

            if (currentList.isNotEmpty() && shelfBooks.isNotEmpty()) {
                hasChange.set(false)
                ExecutorFactory.CPU_BOUND_EXECUTOR.execute {
                    val diffCallback = object : DiffUtil.Callback() {
                        override fun areItemsTheSame(
                            oldItemPosition: Int,
                            newItemPosition: Int
                        ): Boolean {
                            return currentList[oldItemPosition].id == shelfBooks[newItemPosition].id
                        }

                        override fun getOldListSize() = currentList.size
                        override fun getNewListSize() = shelfBooks.size
                        override fun areContentsTheSame(
                            oldItemPosition: Int,
                            newItemPosition: Int
                        ): Boolean {
                            val curVal = currentList[oldItemPosition]
                            val newVal = shelfBooks[newItemPosition]
                            if (curVal === newVal) {
                                return true
                            }

                            return curVal.lastAccessTime == newVal.lastAccessTime
                                    && curVal.filename == newVal.filename
                                    && curVal.recordDesc == newVal.recordDesc
                        }
                    }

                    if (diffCallback.oldListSize == diffCallback.newListSize) {
                        for (i in 0 until diffCallback.oldListSize) {
                            if (!diffCallback.areItemsTheSame(
                                    i,
                                    i
                                ) || !diffCallback.areContentsTheSame(i, i)
                            ) {
                                hasChange.set(true)
                                break
                            }
                        }
                    } else {
                        hasChange.set(true)
                    }

                    if (hasChange.get()) {
                        diffResultRef.set(DiffUtil.calculateDiff(diffCallback))
                    }
                    handler.post(mainTask)
                }
            } else {
                if (currentList.isEmpty() && shelfBooks.isEmpty()) {
                    hasChange.set(false)
                }
                handler.post(mainTask)
            }
        }
    }

    override fun onCleared() {
        BookShelfOpenHelper.instance.removeBookFileInfoUpdateListener(this)
        handler.removeCallbacksAndMessages(null)
        BookManager.instance.removeBookChangeListener(this)
    }

    private fun checkUpdateHasData() {
        val hasData = list.isNotEmpty()
        if (hasData != hasDataLiveData.value) {
            hasDataLiveData.value = hasData
        }
    }

    override fun onBooksImported(list: List<BookFile>) {
        if (list.isNotEmpty()) {
            handler.post { refreshList(fromCallback = true) }
        }
    }

    override fun onBookDeleted() {
        handler.post { refreshList(fromCallback = true) }
    }

    override fun onBookInfoUpdate() {
        handler.post {
            refreshList(true)
        }
    }

    override fun onBookCollectChange(id: String, collect: Boolean) {
        handler.post {
            for (bookShelfBook in list) {
                if (bookShelfBook.id == id) {
                    bookShelfBook.collect = collect
                    return@post
                }
            }
            refreshList(true)
        }
    }

    override fun onBookInfoUpdate(id: String) {
        handler.post {
            refreshList(true)
        }
    }
}
