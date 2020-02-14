package com.lyc.easyreader.bookshelf.utils

import android.annotation.SuppressLint
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.utils.LogUtils
import org.mozilla.universalchardet.CharsetListener
import org.mozilla.universalchardet.UniversalDetector
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.getOrSet

/**
 * Created by Liu Yuchuan on 2020/1/22.
 */

private const val TAG = "FileUtils"

const val ANDROID_DOCUMENT_AUTHORITY = "com.android.externalstorage.documents"

val APP_DEFAULT_CHARSET: Charset = try {
    Charset.forName("GBK")
} catch (e: Exception) {
    Charset.defaultCharset().also {
        LogUtils.d(TAG, "Cannot load GBK charset! Use system default charset: $it", e)
    }
}

fun safeCharsetForName(name: String): Charset {
    return try {
        Charset.forName(name)
    } catch (e: Exception) {
        APP_DEFAULT_CHARSET.also {
            LogUtils.d(TAG, "Cannot create charset for $name! Use app default charset: $it", e)
        }
    }
}

fun Uri?.detectCharset(): Charset {
    if (this == null) {
        return APP_DEFAULT_CHARSET
    }
    ReaderApplication.appContext().contentResolver.openInputStream(this)?.let {
        it.buffered().use { reader ->
            val byte3 = ByteArray(3)
            reader.mark(0)
            var read = reader.read(byte3, 0, 3)
            if (read == -1) {
                return APP_DEFAULT_CHARSET
            }

            if (byte3[0] == 0xEF.toByte() && byte3[1] == 0xBB.toByte() && byte3[2] == 0xBF.toByte()) {
                return Charsets.UTF_8
            }

            reader.mark(0)

            while (reader.read().also { read = it } != -1) {
                if (read >= 0xF0) break
                if (read in 0x80..0xBF) // 单独出现BF以下的，也算是GBK
                    break
                if (read in 0xC0..0xDF) {
                    // 双字节 (0xC0 - 0xDF)
                    read = reader.read()
                    if (read in 0x80..0xBF) {
                        // (0x80 - 0xBF),也可能在GB编码内
                        continue
                    } else {
                        break
                    }
                } else if (read in 0xE0..0xEF) {
                    // 也有可能出错，但是几率较小
                    read = reader.read()
                    if (read in 0x80..0xBF) {
                        read = reader.read()
                        if (read in 0x80..0xBF) {
                            return Charsets.UTF_8
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }
            }
        }
    }

    return APP_DEFAULT_CHARSET
}

fun RandomAccessFile?.detectCharsetUseLib(): Charset {
    if (this == null) {
        return APP_DEFAULT_CHARSET
    }

    val detector = UniversalDetector(CharsetListener { var1 -> println("charset = $var1") })
    val buffer = ByteArray(4096)
    var len: Int
    while (read(buffer).also { len = it } > 0 && !detector.isDone) {
        detector.handleData(buffer, 0, len)
    }
    detector.dataEnd()
    LogUtils.d(TAG, "Charset=${detector.detectedCharset}")
    return if (detector.detectedCharset == null) {
        APP_DEFAULT_CHARSET
    } else {
        safeCharsetForName(detector.detectedCharset)
    }
}

fun RandomAccessFile?.detectCharset(): Charset {
    if (this == null) {
        return APP_DEFAULT_CHARSET
    }

    val byte3 = ByteArray(3)
    var read = read(byte3, 0, 3)
    if (read == -1) {
        return APP_DEFAULT_CHARSET
    }

    if (byte3[0] == 0xEF.toByte() && byte3[1] == 0xBB.toByte() && byte3[2] == 0xBF.toByte()) {
        return Charsets.UTF_8
    }

    while (read().also { read = it } != -1) {
        if (read >= 0xF0) break
        if (read in 0x80..0xBF) // 单独出现BF以下的，也算是GBK
            break
        if (read in 0xC0..0xDF) {
            // 双字节 (0xC0 - 0xDF)
            read = read()
            if (read in 0x80..0xBF) {
                // (0x80 - 0xBF),也可能在GB编码内
                continue
            } else {
                break
            }
        } else if (read in 0xE0..0xEF) {
            // 也有可能出错，但是几率较小
            read = read()
            if (read in 0x80..0xBF) {
                read = read()
                if (read in 0x80..0xBF) {
                    return Charsets.UTF_8
                } else {
                    break
                }
            } else {
                break
            }
        }
    }

    return APP_DEFAULT_CHARSET
}

fun singleUriDocumentFile(uri: Uri) =
    DocumentFile.fromSingleUri(ReaderApplication.appContext(), uri)!!

fun treeDocumentFile(uri: Uri) = DocumentFile.fromTreeUri(ReaderApplication.appContext(), uri)!!

fun DocumentFile.forEach(
    func: (file: DocumentFile) -> Unit,
    onlyFile: Boolean = true,
    recursive: Boolean = true,
    maxDepth: Int = 2,
    cancelToken: AtomicBoolean? = null,
    fastMode: Boolean = true
) {
    if (!recursive) {
        forEachSubFile(func, onlyFile, cancelToken)
        return
    }

    if (fastMode) {
        forEachFileRecursivelyFastMode(func, onlyFile, maxDepth, cancelToken)
    } else {
        forEachFileRecursivelyBfs(func, onlyFile, maxDepth, cancelToken)
    }
}

private inline fun DocumentFile.forEachSubFile(
    func: (file: DocumentFile) -> Unit,
    onlyFile: Boolean,
    cancelToken: AtomicBoolean?
) {
    if (isDirectory) {
        listFiles().forEach {
            if (cancelToken?.get() == true) {
                return
            }
            if (!onlyFile || (onlyFile && !it.isDirectory)) {
                func(it)
            }
        }
    }
}

private fun DocumentFile.forEachFileRecursivelyFastMode(
    func: (file: DocumentFile) -> Unit,
    onlyFile: Boolean,
    maxDepth: Int,
    cancelToken: AtomicBoolean?
) {
    val list = CopyOnWriteArrayList<DocumentFile>()
    list.add(this)
    var currentDepth = 0
    while (list.isNotEmpty()) {
        val currentList = list.toList()
        list.clear()
        val latch = CountDownLatch(currentList.size)
        for (documentFile in currentList) {
            if (cancelToken?.get() == true) {
                return
            }
            ExecutorFactory.IO_EXECUTOR.execute {
                try {
                    if (cancelToken?.get() == true) {
                        return@execute
                    }
                    if (documentFile.isDirectory) {
                        if (!onlyFile) {
                            func(documentFile)
                        } else if (currentDepth < maxDepth || maxDepth <= 0) {
                            list.addAll(documentFile.listFiles())
                        }
                    } else {
                        func(documentFile)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }
        latch.await()
        currentDepth++
    }
}

private inline fun DocumentFile.forEachFileRecursivelyBfs(
    func: (file: DocumentFile) -> Unit,
    onlyFile: Boolean,
    maxDepth: Int,
    cancelToken: AtomicBoolean?
) {
    val list = LinkedList<Pair<Int, DocumentFile>>()
    list.add(Pair(0, this))

    while (list.isNotEmpty()) {
        if (cancelToken?.get() == true) {
            return
        }
        val cur = list.poll()!!
        val file = cur.second
        if (!onlyFile || (onlyFile && !file.isDirectory)) {
            func(file)
        }
        if (file.isDirectory && (cur.first < maxDepth || maxDepth <= 0)) {
            val nextDepth = cur.first + 1
            list.addAll(file.listFiles().map { Pair(nextDepth, it) })
        }
    }
}

fun DocumentFile.getExt(): String {
    val name = name ?: ""
    return name.substringAfterLast(".", "")
}

fun Long.toFileSizeString(): String {
    if (this < 1024) {
        return "${this}B"
    }

    val kb = this shr 10

    if (kb < 2014) {
        return "%.2fKB".format(this.toDouble() / 1024.0)
    }

    val mb = kb shr 10
    if (mb < 1024) {
        return "%.2fMB".format(this.toDouble() / (1024.0 * 1024.0))
    }

    return "%.2fGB".format(this.toDouble() / (1024.0 * 1024.0 * 2014.0))
}

private val timeDateFormatThreadLocal = ThreadLocal<SimpleDateFormat>()

@SuppressLint("SimpleDateFormat")
fun Long.toFileTimeString(): String = timeDateFormatThreadLocal.getOrSet {
    SimpleDateFormat("yyyy-MM-dd")
}.format(this)

private val detailTimeDateFormatThreadLocal = ThreadLocal<SimpleDateFormat>()
@SuppressLint("SimpleDateFormat")
fun Long.detailTimeString(): String = timeDateFormatThreadLocal.getOrSet {
    SimpleDateFormat("yyyy-MM-dd HH:mm")
}.format(this)
