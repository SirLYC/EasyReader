package com.lyc.bookshelf

import android.net.Uri
import android.os.SystemClock
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import com.lyc.api.book.BookFile
import com.lyc.api.book.IBookManager
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ServiceImpl
import com.lyc.base.ReaderApplication
import com.lyc.base.ui.ReaderToast
import com.lyc.base.utils.LogUtils
import com.lyc.bookshelf.db.BookShelfOpenHelper
import com.lyc.bookshelf.utils.toFileSizeString
import com.lyc.common.EventHubFactory
import com.lyc.common.thread.ExecutorFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Liu Yuchuan on 2020/1/26.
 */
@ServiceImpl(service = IBookManager::class, createMethod = CreateMethod.GET_INSTANCE)
class BookManager private constructor() : IBookManager {
    companion object {
        @JvmStatic
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { BookManager() }

        const val TAG = "BookManager"
    }

    private val eventHub = EventHubFactory.createDefault<IBookManager.IBookChangeListener>(true)

    override fun addBookChangeListener(listener: IBookManager.IBookChangeListener) {
        eventHub.addEventListener(listener)
    }

    override fun removeBookChangeListener(listener: IBookManager.IBookChangeListener) {
        eventHub.removeEventListener(listener)
    }

    @AnyThread
    override fun importBooks(uriList: List<Uri>) {
        if (uriList.isEmpty()) {
            return
        }

        val start = SystemClock.elapsedRealtime()
        val progressCount = AtomicInteger(uriList.size)
        val failCount = AtomicInteger(0)
        val newBooks = CopyOnWriteArrayList<BookFile>()

        for (uri in uriList) {
            ExecutorFactory.IO_EXECUTOR.execute {
                doImportOneBook(uri, { bookFile, _ ->
                    newBooks.add(bookFile)
                    if (progressCount.decrementAndGet() == 0) {
                        val faiCnt = failCount.get()
                        LogUtils.i(
                            TAG,
                            "Import ${uriList.size} books costs ${SystemClock.elapsedRealtime() - start}ms."
                        )
                        handleImportBooksFinished(faiCnt, newBooks.toList())
                    }
                }, { _, _ ->
                    failCount.incrementAndGet()
                    if (progressCount.decrementAndGet() == 0) {
                        val faiCnt = failCount.get()
                        LogUtils.i(
                            TAG,
                            "Import ${uriList.size} books costs ${SystemClock.elapsedRealtime() - start}ms."
                        )
                        handleImportBooksFinished(faiCnt, newBooks.toList())
                    }
                })
            }
        }
    }

    private fun handleImportBooksFinished(
        faiCnt: Int,
        newBookList: List<BookFile>
    ) {

        val sucCnt = newBookList.size
        if (sucCnt <= 0 && faiCnt <= 0) {
            return
        }

        if (sucCnt <= 0) {
            ReaderToast.showToast("导入失败")
            return
        }

        ReaderToast.showToast(
            if (faiCnt <= 0) {
                if (sucCnt == 1) "导入成功" else "成功导入${sucCnt}个文件"
            } else {
                "成功导入${sucCnt}个文件，失败${faiCnt}"
            }
        )
        eventHub.getEventListeners().forEach {
            it.onBooksImported(newBookList)
        }
    }

    @WorkerThread
    private inline fun doImportOneBook(
        uri: Uri,
        onFinish: (BookFile, Uri) -> Unit,
        onError: (String, Uri) -> Unit
    ): Boolean {

        val startTime = SystemClock.elapsedRealtime()
        var fileSize = 0L
        var finish = false
        var file: File? = null
        try {
            val currentTime = System.currentTimeMillis()
            val newBookFile =
                BookFile(
                    null,
                    null,
                    null,
                    currentTime,
                    currentTime,
                    0,
                    BookFile.Status.TMP
                )
            BookShelfOpenHelper.instance.insertBookFile(newBookFile)
            val bookId = newBookFile.id
            if (bookId == null || bookId <= 0) {
                val reason = "Insert database error, id=${bookId}(uri=${uri})."
                onError(reason, uri)
                LogUtils.e(TAG, reason)
            }
            val fd = ReaderApplication.appContext().contentResolver.openFileDescriptor(uri, "r")
            if (fd == null) {
                val reason = "Open uri(${uri}) error."
                onError(reason, uri)
                LogUtils.e(TAG, reason)
                return false
            }

            fd.use {
                val size = fd.statSize.also { fileSize = it }
                if (size <= 0) {
                    val reason = "File(uri=${uri}) size <= 0."
                    onError(reason, uri)
                    LogUtils.e(TAG, reason)
                    return false
                }

                val outputDir = ReaderApplication.appContext().getExternalFilesDir(".books")
                if (outputDir == null || (!outputDir.exists() && !outputDir.mkdirs())) {
                    val reason = "Cannot get external .books dir!"
                    onError(reason, uri)
                    LogUtils.e(TAG, reason)
                    return false
                }
                val filename = ".Book_${bookId}"
                val outputFile = File(outputDir, filename)
                LogUtils.i(TAG, "Copy file from uri $outputDir to file $outputFile")

                val bufferSize = if (size < 4 shl 10) {
                    size.toInt()
                } else {
                    4 shl 10
                }

                val buffer = ByteArray(bufferSize)
                FileInputStream(fd.fileDescriptor).buffered().use { inputStream ->
                    var readSize: Int
                    file = outputFile
                    FileOutputStream(outputFile).use { outputStream ->
                        while (inputStream.read(buffer, 0, bufferSize).also {
                                readSize = it
                            } != -1) {
                            outputStream.write(buffer, 0, readSize)
                        }
                    }
                }

                newBookFile.filename = filename
                newBookFile.realPath = outputFile.absolutePath
                newBookFile.status = BookFile.Status.NORMAL
                BookShelfOpenHelper.instance.insertOrReplaceBookFile(newBookFile)
                onFinish(newBookFile, uri)
                finish = true
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, null, e)
            onError(e.message ?: "", uri)
        } finally {
            if (!finish) {
                file?.delete()
            }
        }

        LogUtils.i(
            TAG,
            "Finish import costs ${SystemClock.elapsedRealtime() - startTime}ms, fileSize=${fileSize.toFileSizeString()}"
        )

        return finish
    }
}
