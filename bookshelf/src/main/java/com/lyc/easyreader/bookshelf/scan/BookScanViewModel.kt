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
import com.lyc.easyreader.bookshelf.utils.forEach
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
    val list = ObservableList<Any>(arrayListOf())
    val selectController = PositionSelectController()
    var activityVisible = false
    @Volatile
    var alive = false
    var pendingToast: String? = null

    companion object {
        const val KEY_URI = "KEY_URI"

        const val TAG = "BookScanViewModel"
    }

    fun changeActivityVisibility(visible: Boolean) {
        if (activityVisible != visible) {
            activityVisible = visible
            if (visible) {
                pendingToast?.let {
                    ReaderHeadsUp.showHeadsUp(it)
                    pendingToast = null
                }
            }
        }
    }

    private fun showToastIfVisible(toast: String) {
        if (activityVisible) {
            ReaderHeadsUp.showHeadsUp(toast)
        } else {
            pendingToast = toast
        }
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
            list.add(EmptyItem)
            scanningLiveData.value = false
            return
        }

        val scanInvisibleFile = ScanSettings.scanInvisibleFile.value
        val enableFilter = ScanSettings.enableFilter.value
        val filters = ScanSettings.filterSet.value.toSet()
        val depth = ScanSettings.scanDepth.value.depth
        ExecutorFactory.CPU_BOUND_EXECUTOR.execute {
            if (!alive) {
                return@execute
            }
            val lock = ReentrantLock()
            val tmpList = LinkedList<BookScanItem>()
            treeDocumentFile(uriLocal).forEach({ file ->
                val filename = file.name ?: return@forEach
                if (!scanInvisibleFile && filename.startsWith(".")) {
                    return@forEach
                }
                val lowerCase = filename.toLowerCase(Locale.ENGLISH)
                if (enableFilter && filters.any { lowerCase.contains(it) }) {
                    return@forEach
                }
                val ext = filename.substringAfterLast(".", "")
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                if (mimeType?.startsWith("text/") == true && file.length() > 0) {
                    val bookScanItem =
                        BookScanItem(filename, ext, file.uri, file.lastModified(), file.length())
                    LogUtils.d(TAG, "Scan a book: $bookScanItem")
                    lock.withLock {
                        tmpList.add(bookScanItem)
                        if (tmpList.size >= 5) {
                            notifyAppendList(tmpList)
                        }
                    }
                }
            }, maxDepth = depth, cancelToken = idle)
            idle.set(true)
            if (tmpList.isNotEmpty()) {
                notifyAppendList(tmpList)
            }
            ExecutorFactory.MAIN_EXECUTOR.execute {
                if (alive) {
                    if (list.isEmpty()) {
                        showToastIfVisible("没有扫描到相关文件")
                        list.add(EmptyItem)
                    } else {
                        showToastIfVisible("扫描到${list.size}本书，可多选导入")
                    }
                    scanFinishLiveData.value = true
                    scanningLiveData.value = false
                    LogUtils.d(TAG, "Book scan finished, size=${list.size}")
                }
            }
        }
    }

    private fun notifyAppendList(newItems: LinkedList<BookScanItem>) {
        val copiedValues = newItems.toArray()
        newItems.clear()
        ExecutorFactory.MAIN_EXECUTOR.execute {
            if (alive) {
                list.addAll(copiedValues)
            }
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
            return list[0] !== EmptyItem
        }

        return true
    }

    override fun onCleared() {
        alive = false
        super.onCleared()
        idle.set(true)
    }
}
