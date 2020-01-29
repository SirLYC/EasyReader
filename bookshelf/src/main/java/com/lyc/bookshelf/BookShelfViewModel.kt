package com.lyc.bookshelf

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import com.lyc.api.book.BookFile
import com.lyc.api.book.IBookManager
import com.lyc.base.arch.NonNullLiveData
import com.lyc.base.ui.ReaderHeadsUp
import com.lyc.base.utils.rv.ObservableList
import com.lyc.bookshelf.db.BookShelfOpenHelper
import com.lyc.common.thread.ExecutorFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class BookShelfViewModel : ViewModel(), IBookManager.IBookChangeListener {
    private val handler = Handler(Looper.getMainLooper())

    val hasDataLiveData = MutableLiveData<Boolean>()
    val isLoadingLiveData = NonNullLiveData(false)
    val list = ObservableList(arrayListOf<Pair<Int, Any>>())
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
    fun refreshList() {
        if (isLoadingLiveData.value) {
            return
        }
        isLoadingLiveData.value = true
        val currentList = list.map { it.second as BookFile }
        ExecutorFactory.IO_EXECUTOR.execute {
            val shelfBooks = BookShelfOpenHelper.instance.loadBookShelfList()
            val diffResultRef = AtomicReference<DiffUtil.DiffResult>(null)
            val hasChange = AtomicBoolean(true)
            val mainTask = Runnable {
                val diffResult: DiffUtil.DiffResult? = diffResultRef.get()
                val resultList = shelfBooks.map { Pair<Int, Any>(0, it) }
                if (diffResult == null) {
                    if (hasChange.get()) {
                        list.replaceAll(resultList)
                        if (firstLoadFinish) {
                            ReaderHeadsUp.showHeadsUp("刷新完成")
                        }
                    } else {
                        ReaderHeadsUp.showHeadsUp("没有更新")
                    }
                } else {
                    list.withoutCallback {
                        list.clear()
                        list.addAll(resultList)
                    }
                    diffResult.dispatchUpdatesTo(list)
                    if (firstLoadFinish) {
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
                    }

                    if (hasChange.get()) {
                        diffResultRef.set(DiffUtil.calculateDiff(diffCallback))
                    }
                    handler.post(mainTask)
                }
            } else {
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
            handler.post { refreshList() }
        }
    }
}
