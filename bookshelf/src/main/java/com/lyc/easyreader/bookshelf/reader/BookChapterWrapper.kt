package com.lyc.easyreader.bookshelf.reader

import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.bookshelf.utils.APP_DEFAULT_CHARSET
import java.io.BufferedReader
import java.io.RandomAccessFile
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Created by Liu Yuchuan on 2020/2/2.
 */
class BookChapterWrapper(
    private val entity: BookChapter,
    private val bookFile: BookFile
) {

    companion object {
        const val TAG = "BookChapterWrapper"
    }

    val memorySize = (entity.end - entity.start).toInt()

    @Volatile
    private var content: ByteArray? = null

    private val contentLock = ReentrantLock()

    private fun loadContent(): ByteArray? {
        val charset = bookFile.charset
        if (charset == null || bookFile.handleChapterLastModified <= 0) {
            return null
        }

        try {
            return RandomAccessFile(bookFile.realPath, "r").use { raf ->
                raf.seek(entity.start)
                val totalSize = memorySize
                val buffer = ByteArray(totalSize)
                var totalRead = 0
                var currentRead: Int

                while (totalRead < totalSize) {
                    currentRead = raf.read(buffer, totalRead, totalSize - totalRead)
                    totalRead += currentRead
                }
                buffer
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, ex = e)
            return null
        }
    }

    private fun getLoadedContent(): ByteArray? {
        if (content == null) {
            contentLock.withLock {
                if (content == null) {
                    content = loadContent()
                }
            }
        }
        return content
    }

    fun openBufferedReader(): BufferedReader? {
        val bytes = getLoadedContent() ?: return null
        return bytes.inputStream().bufferedReader(bookFile.charset ?: APP_DEFAULT_CHARSET)
    }
}
