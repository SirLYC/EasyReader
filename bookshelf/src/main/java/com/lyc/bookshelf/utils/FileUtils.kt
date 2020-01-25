package com.lyc.bookshelf.utils

import android.annotation.SuppressLint
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.lyc.base.ReaderApplication
import com.lyc.base.utils.LogUtils
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
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

fun singleUriDocumentFile(uri: Uri) =
    DocumentFile.fromSingleUri(ReaderApplication.appContext(), uri)!!

fun treeDocumentFile(uri: Uri) = DocumentFile.fromTreeUri(ReaderApplication.appContext(), uri)!!

fun DocumentFile.forEach(
    func: (file: DocumentFile) -> Unit,
    onlyFile: Boolean = true,
    recursive: Boolean = true,
    maxDepth: Int = 2,
    cancelToken: AtomicBoolean? = null
) {
    if (!recursive) {
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
        return
    }

    if (maxDepth <= 0) {
        throw IllegalArgumentException("Param \"maxDepth\" must be positive!")
    }

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
        if (file.isDirectory && cur.first < maxDepth) {
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