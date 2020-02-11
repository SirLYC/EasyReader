package com.lyc.easyreader.bookshelf.reader


import androidx.core.util.lruCache
import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.base.utils.getMd5

/**
 * Created by Liu Yuchuan on 2020/2/2.
 */
class BookChapterCache {
    companion object {
        const val TAG = "BookChapterCache"

        fun maxBookChapterCache(): Int {
            val memory = Runtime.getRuntime().maxMemory()
            if (memory <= 0) {
                return 10 shl 20
            }

            return (memory / 2).toInt()
        }
    }

    private val cache = lruCache<String, BookChapterWrapper>(
        maxBookChapterCache(),
        { _, value ->
            value.memorySize
        })

    fun evict(bookChapter: BookChapter) {
        cache.remove(calculateKey(bookChapter))
    }

    fun put(bookChapter: BookChapter, value: BookChapterWrapper) {
        cache.put(calculateKey(bookChapter), value)
    }

    fun get(bookChapter: BookChapter): BookChapterWrapper? =
        cache.get(calculateKey(bookChapter))

    private fun calculateKey(bookChapter: BookChapter) =
        "$TAG&${bookChapter.bookId}&${bookChapter.lastModified}&${bookChapter.order}&${bookChapter.title}".getMd5()
}
