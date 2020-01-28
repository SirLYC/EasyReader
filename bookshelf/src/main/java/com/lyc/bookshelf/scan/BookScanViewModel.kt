package com.lyc.bookshelf.scan

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.lyc.base.arch.NonNullLiveData
import com.lyc.base.utils.LogUtils
import com.lyc.base.utils.rv.ObservableList
import com.lyc.bookshelf.VIEW_TYPE_EMPTY_ITEM
import com.lyc.bookshelf.VIEW_TYPE_SCAN_ITEM
import com.lyc.bookshelf.utils.forEach
import com.lyc.bookshelf.utils.getExt
import com.lyc.bookshelf.utils.treeDocumentFile
import com.lyc.common.thread.ExecutorFactory
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Liu Yuchuan on 2020/1/24.
 */
class BookScanViewModel : ViewModel() {
    var uri: Uri? = null
    private val scanFinishLiveData = NonNullLiveData(false)
    val scanningLiveData = NonNullLiveData(false)
    private val idle = AtomicBoolean(true)
    val list = ObservableList<Pair<Int, Any>>(arrayListOf())
    val selectController = PositionSelectController()

    companion object {
        const val KEY_URI = "KEY_URI"

        const val TAG = "BookScanViewModel"
    }

    fun startScan() {
        if (scanFinishLiveData.value || !idle.compareAndSet(true, false)) {
            return
        }

        scanningLiveData.value = true

        LogUtils.d(TAG, "Book scan started. uri=$uri")

        list.clear()
        val uriLocal = uri
        if (uriLocal == null) {
            scanFinishLiveData.value = true
            list.add(Pair(VIEW_TYPE_EMPTY_ITEM, EmptyItem))
            scanningLiveData.value = false
            return
        }

        ExecutorFactory.IO_EXECUTOR.execute {
            val tmpList = LinkedList<BookScanItem>()
            treeDocumentFile(uriLocal).forEach({ file ->
                val ext = file.getExt()
                if (ext.toLowerCase(Locale.getDefault()) == "txt" && file.length() > 0) {
                    val bookScanItem =
                        BookScanItem(file.name, ext, file.uri, file.lastModified(), file.length())
                    LogUtils.d(TAG, "Scan a book: $bookScanItem")
                    tmpList.add(bookScanItem)
                }

                if (tmpList.size >= 5) {
                    notifyAppendList(tmpList)
                }
            }, cancelToken = idle)
            idle.set(true)
            if (tmpList.isNotEmpty()) {
                notifyAppendList(tmpList)
            }
            ExecutorFactory.MAIN_EXECUTOR.execute {
                if (list.isEmpty()) {
                    list.add(Pair(VIEW_TYPE_EMPTY_ITEM, EmptyItem))
                }
                scanFinishLiveData.value = true
                scanningLiveData.value = false
                LogUtils.d(TAG, "Book scan finished, size=${list.size}")
            }
        }
    }

    private fun notifyAppendList(newItems: LinkedList<BookScanItem>) {
        val copiedValues = newItems.toArray()
        newItems.clear()
        ExecutorFactory.MAIN_EXECUTOR.execute {
            list.addAll(copiedValues.map { Pair(VIEW_TYPE_SCAN_ITEM, it) })
        }
    }

    fun stopScan() {
        if (idle.compareAndSet(false, true)) {
            LogUtils.d(TAG, "Stopped by stopScan()")
        }
    }

    fun saveState(bundle: Bundle) {
        bundle.putParcelable(KEY_URI, uri)
    }

    fun restoreState(bundle: Bundle) {
        uri = bundle.getParcelable(KEY_URI)
    }

    fun hasFile(): Boolean {
        if (list.isEmpty()) {
            return true
        }

        if (list.size == 1) {
            return list[0].second !== EmptyItem
        }

        return true
    }

    override fun onCleared() {
        super.onCleared()
        idle.set(true)
    }
}
