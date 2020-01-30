package com.lyc.easyreader.bookshelf.scan

import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.ui.ReaderHeadsUp
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.bookshelf.VIEW_TYPE_EMPTY_ITEM
import com.lyc.easyreader.bookshelf.VIEW_TYPE_SCAN_ITEM
import com.lyc.easyreader.bookshelf.utils.forEach
import com.lyc.easyreader.bookshelf.utils.getExt
import com.lyc.easyreader.bookshelf.utils.treeDocumentFile
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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

        ExecutorFactory.CPU_BOUND_EXECUTOR.execute {
            val lock = ReentrantLock()
            val tmpList = LinkedList<BookScanItem>()
            treeDocumentFile(uriLocal).forEach({ file ->
                val ext = file.getExt()
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                if (mimeType?.startsWith("text/") == true && file.length() > 0) {
                    val bookScanItem =
                        BookScanItem(file.name, ext, file.uri, file.lastModified(), file.length())
                    LogUtils.d(TAG, "Scan a book: $bookScanItem")
                    lock.withLock {
                        tmpList.add(bookScanItem)
                        if (tmpList.size >= 5) {
                            notifyAppendList(tmpList)
                        }
                    }
                }
            }, cancelToken = idle)
            idle.set(true)
            if (tmpList.isNotEmpty()) {
                notifyAppendList(tmpList)
            }
            ExecutorFactory.MAIN_EXECUTOR.execute {
                if (list.isEmpty()) {
                    ReaderHeadsUp.showHeadsUp("没有扫描到相关文件")
                    list.add(Pair(VIEW_TYPE_EMPTY_ITEM, EmptyItem))
                } else {
                    ReaderHeadsUp.showHeadsUp("扫描到${list.size}本书，可多选导入")
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
