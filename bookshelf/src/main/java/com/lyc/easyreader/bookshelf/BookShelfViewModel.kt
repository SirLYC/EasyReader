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
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class BookShelfViewModel : ViewModel(), IBookManager.IBookChangeListener {
    private val handler = Handler(Looper.getMainLooper())

    val hasDataLiveData = MutableLiveData<Boolean>()
    val isLoadingLiveData = NonNullLiveData(false)
    val list = ObservableList(arrayListOf<BookFile>())
    var firstLoadFinish = false
        private set

    init {
        BookManager.instance.addBookChangeListener(this)
    }

    fun firstLoadIfNeeded() {
        if (firstLoadFinish || isLoadingLiveData.value) {
            return
        }
        refreshList()
    }

    @MainThread
    fun refreshList(fromCallback: Boolean = false) {
        if (isLoadingLiveData.value) {
            return
        }
        isLoadingLiveData.value = true
        val currentList = list.toList()
        ExecutorFactory.IO_EXECUTOR.execute {
            val shelfBooks = BookShelfOpenHelper.instance.loadBookShelfList()
            val diffResultRef = AtomicReference<DiffUtil.DiffResult>(null)
            val hasChange = AtomicBoolean(true)
            val mainTask = Runnable {
                val diffResult: DiffUtil.DiffResult? = diffResultRef.get()
                if (diffResult == null) {
                    if (hasChange.get()) {
                        list.replaceAll(shelfBooks)
                        if (firstLoadFinish && !fromCallback) {
                            ReaderHeadsUp.showHeadsUp("刷新完成")
                        }
                    } else {
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
                            if (currentList[oldItemPosition] === shelfBooks[newItemPosition]) {
                                return true
                            }

                            return currentList[oldItemPosition].lastAccessTime == shelfBooks[newItemPosition].lastAccessTime
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
}
