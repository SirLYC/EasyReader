package com.lyc.easyreader.bookshelf

import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ServiceImpl
import com.lyc.common.EventHubFactory
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.api.book.IBookManager
import com.lyc.easyreader.api.main.Schema
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.ui.ReaderHeadsUp
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.base.utils.getMd5
import com.lyc.easyreader.base.utils.toHexString
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper
import com.lyc.easyreader.bookshelf.reader.ReaderActivity
import com.lyc.easyreader.bookshelf.utils.toFileSizeString
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.*
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

    override fun alterBookName(id: String, newName: String, async: Boolean) {
        BookShelfOpenHelper.instance.alterBookName(id, newName, async) {
            ReaderToast.showToast("修改成功")
            eventHub.getEventListeners().forEach {
                it.onBookInfoUpdate(id)
            }
        }
    }

    override fun shareBookFile(bookFile: BookFile) {
        val context = ReaderApplication.appContext()
        val uri = FileProvider.getUriForFile(
            context,
            Schema.FILE_PROVIDER_AUTH,
            File(bookFile.realPath)
        )
        val intent = Intent().apply {
            setDataAndType(
                uri,
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(bookFile.fileExt)
            )
            action = Intent.ACTION_SEND
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(Intent.createChooser(intent, "分享到...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            LogUtils.e(ReaderActivity.TAG, ex = e)
        }
    }

    override fun openBookFileByOther(bookFile: BookFile) {
        val context = ReaderApplication.appContext()
        val uri = FileProvider.getUriForFile(
            context,
            Schema.FILE_PROVIDER_AUTH,
            File(bookFile.realPath)
        )
        val intent = Intent().apply {
            setDataAndType(
                uri,
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(bookFile.fileExt)
            )
            action = Intent.ACTION_VIEW
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(Intent.createChooser(intent, "选择APP打开").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            LogUtils.e(ReaderActivity.TAG, ex = e)
        }
    }

    @AnyThread
    override fun importBooks(uriList: List<Uri>) {
        if (uriList.isEmpty()) {
            return
        }

        val start = SystemClock.elapsedRealtime()
        val progressCount = AtomicInteger(uriList.size)
        val failCount = AtomicInteger(0)
        val repeatCount = AtomicInteger(0)
        val newBooks = CopyOnWriteArrayList<BookFile>()

        for (uri in uriList) {
            ExecutorFactory.IO_EXECUTOR.execute {
                doImportOneBook(uri, { bookFile, _ ->
                    newBooks.add(bookFile)
                    if (progressCount.decrementAndGet() == 0) {
                        val faiCnt = failCount.get()
                        val repeatCnt = repeatCount.get()
                        LogUtils.i(
                            TAG,
                            "Import ${uriList.size} books costs ${SystemClock.elapsedRealtime() - start}ms."
                        )
                        handleImportBooksFinished(faiCnt, repeatCnt, newBooks.toList())
                    }
                }, { _, _ ->
                    failCount.incrementAndGet()
                    if (progressCount.decrementAndGet() == 0) {
                        val faiCnt = failCount.get()
                        val repeatCnt = repeatCount.get()
                        LogUtils.i(
                            TAG,
                            "Import ${uriList.size} books costs ${SystemClock.elapsedRealtime() - start}ms."
                        )
                        handleImportBooksFinished(faiCnt, repeatCnt, newBooks.toList())
                    }
                }, { _, _ ->
                    repeatCount.incrementAndGet()
                    if (progressCount.decrementAndGet() == 0) {
                        val faiCnt = failCount.get()
                        val repeatCnt = repeatCount.get()
                        LogUtils.i(
                            TAG,
                            "Import ${uriList.size} books costs ${SystemClock.elapsedRealtime() - start}ms."
                        )
                        handleImportBooksFinished(faiCnt, repeatCnt, newBooks.toList())
                    }
                })
            }
        }
    }

    override fun importBookAndOpen(uri: Uri) {
        ExecutorFactory.IO_EXECUTOR.execute {
            doImportOneBook(uri, { bookFile, _ ->
                ReaderToast.showToast("导入成功")
                ExecutorFactory.MAIN_EXECUTOR.execute {
                    ReaderActivity.openBookFile(bookFile)
                }
            }, { _, _ ->
                ReaderHeadsUp.showHeadsUp("导入失败")
            }, { _, bookFile ->
                if (bookFile != null) {
                    ExecutorFactory.MAIN_EXECUTOR.execute {
                        ReaderActivity.openBookFile(bookFile)
                    }
                }
            })
        }

    }

    override fun updateBookCollect(id: String, collect: Boolean, async: Boolean) {
        BookShelfOpenHelper.instance.updateBookCollect(id, collect, async) {
            eventHub.getEventListeners().forEach {
                it.onBookCollectChange(id, collect)
                ReaderToast.showToast(if (collect) "已收藏" else "已取消收藏")
            }
        }
    }

    override fun deleteBook(id: String, async: Boolean) {
        BookShelfOpenHelper.instance.deleteBookFile(id, async) {
            eventHub.getEventListeners().forEach {
                it.onBookDeleted()
                ReaderToast.showToast("已删除")
            }
        }
    }

    override fun deleteBooks(ids: Iterable<String>, async: Boolean) {
        BookShelfOpenHelper.instance.deleteBookFiles(ids, async) {
            eventHub.getEventListeners().forEach {
                it.onBookDeleted()
                ReaderToast.showToast("已删除")
            }
        }
    }

    private fun handleImportBooksFinished(
        faiCnt: Int,
        repeatCnt: Int,
        newBookList: List<BookFile>
    ) {

        val sucCnt = newBookList.size
        if (sucCnt <= 0 && faiCnt <= 0 && repeatCnt <= 0) {
            return
        }

        if (sucCnt <= 0 && faiCnt <= 0 && repeatCnt > 0) {
            ReaderHeadsUp.showHeadsUp(if (repeatCnt == 1) "文件已经在书架，无需导入" else "${repeatCnt}个文件已经在书架，无需导入")
            return
        }

        if (sucCnt <= 0) {
            ReaderHeadsUp.showHeadsUp("导入失败")
            return
        }

        ReaderHeadsUp.showHeadsUp(
            if (faiCnt <= 0) {
                if (sucCnt == 1) "导入成功" else "成功导入${sucCnt}个文件"
            } else {
                "成功导入${sucCnt}个文件，失败${faiCnt}个"
            } + if (repeatCnt == 0) "" else "，${repeatCnt}个文件已经在书架，无需导入"
        )
        eventHub.getEventListeners().forEach {
            it.onBooksImported(newBookList)
        }
    }

    @WorkerThread
    private inline fun doImportOneBook(
        uri: Uri,
        onFinish: (BookFile, Uri) -> Unit,
        onError: (String, Uri) -> Unit,
        onRepeatImport: (Uri, BookFile?) -> Unit
    ): Boolean {

        val startTime = SystemClock.elapsedRealtime()
        var fileSize = 0L
        var finish = false
        var file: File? = null
        try {
            val currentTime = System.currentTimeMillis()
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
                val randomString = "${SystemClock.elapsedRealtimeNanos()}&${UUID.randomUUID()}"
                val filename = ".Book_${randomString.getMd5()}"
                val outputFile = File(outputDir, filename)
                LogUtils.i(TAG, "Copy file from uri $outputDir to file $outputFile")

                val bufferSize = if (size < 4 shl 10) {
                    size.toInt()
                } else {
                    4 shl 10
                }

                val buffer = ByteArray(bufferSize)

                var md5Cost = SystemClock.elapsedRealtime()
                val msgDigest = MessageDigest.getInstance("MD5")
                md5Cost = SystemClock.elapsedRealtime() - md5Cost
                FileInputStream(fd.fileDescriptor).buffered().use { inputStream ->
                    var readSize: Int
                    file = outputFile
                    FileOutputStream(outputFile).use { outputStream ->
                        while (inputStream.read(buffer, 0, bufferSize).also {
                                readSize = it
                            } != -1) {
                            val start = SystemClock.elapsedRealtime()
                            msgDigest.update(buffer, 0, readSize)
                            md5Cost += SystemClock.elapsedRealtime() - start
                            outputStream.write(buffer, 0, readSize)
                        }
                    }
                }

                val start = SystemClock.elapsedRealtime()
                val digest = msgDigest.digest()
                md5Cost += SystemClock.elapsedRealtime() - start
                val md5 = digest.toHexString()
                LogUtils.d(TAG, "File md5=$md5, calculate md5 costs ${md5Cost}ms")

                val orgFilename = getFileNameFromUri(uri)
                val newBookFile =
                    BookFile(
                        outputFile.absolutePath,
                        orgFilename.substringBeforeLast("."),
                        orgFilename.substringAfterLast(".", "txt"),
                        currentTime,
                        0,
                        0,
                        0,
                        null,
                        BookFile.Status.NORMAL,
                        md5
                    )
                if (BookShelfOpenHelper.instance.insertBookFile(newBookFile)) {
                    onFinish(newBookFile, uri)
                    finish = true
                } else {
                    onRepeatImport(uri, BookShelfOpenHelper.instance.loadBookFileById(md5))
                }
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

    private fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            ReaderApplication.appContext().contentResolver.query(uri, null, null, null, null)
                .use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
        }

        return (result ?: uri.path!!.substringAfterLast(File.separator))
    }
}
