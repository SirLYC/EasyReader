package com.lyc.easyreader.bookshelf.reader

import android.os.SystemClock
import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper
import com.lyc.easyreader.bookshelf.utils.ByteCountingLineReader
import com.lyc.easyreader.bookshelf.utils.detectCharset
import com.lyc.easyreader.bookshelf.utils.toFileSizeString
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
class BookParser(private val bookFile: BookFile) {

    companion object {

        const val TAG = "BookParser"

        private const val BUFFER_SIZE = 512 * 1024

        private const val CHAR_BUFFER_SIZE = 1000000

        private const val MAX_LENGTH_WITH_NO_CHAPTER = 10 * 1024L

        //正则表达式章节匹配模式
        // "(第)([0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10})([章节回集卷])(.*)"
        private val CHAPTER_PATTERNS = arrayOf(
            "^(.{0,8})(\u7b2c)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\u7ae0\u8282\u56de\u96c6\u5377])(.{0,30})$",
            "^(\\s{0,4})([\\(\u3010\u300a]?(\u5377)?)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\\.:\uff1a\u0020\u000C\t])(.{0,30})$",
            "^(\\s{0,4})([\\(\uff08\u3010\u300a])(.{0,30})([\\)\uff09\u3011\u300b])(\\s{0,2})$",
            "^(\\s{0,4})(\u6b63\u6587)(.{0,20})$",
            "^(.{0,4})(Chapter|chapter)(\\s{0,4})([0-9]{1,4})(.{0,30})$"
        )

        const val CODE_SUCCESS = -5

        const val CODE_FILE_NOT_EXIST = -2

        const val CODE_FILE_OPEN_FAILED = -5

        const val CODE_FILE_TOO_SMALL = -9
    }

    sealed class ParseChapterResult(
        val code: Int,
        val msg: String,
        val exception: Exception?,
        val list: List<BookChapter>? = null
    ) {
        object FileNotExist : ParseChapterResult(CODE_FILE_NOT_EXIST, "文件不存在", null)
        class FileOpenFail(exception: Exception?) :
            ParseChapterResult(CODE_FILE_OPEN_FAILED, "文件打开失败", exception)

        class FileTooSmall(len: Long) :
            ParseChapterResult(CODE_FILE_TOO_SMALL, "文件过小[${len}]", null)

        class Success(list: List<BookChapter>) :
            ParseChapterResult(CODE_SUCCESS, "", null, list)
    }

    /**
     * 对文件进行分章节
     */
    fun parseChapters(): ParseChapterResult {
        val chapters = mutableListOf<BookChapter>()
        val file = bookFile.realPath.let { if (it.isEmpty()) null else File(it) }
        if (file?.exists() != true) {
            return ParseChapterResult.FileNotExist
        }

        val fileLen = file.length()
        if (fileLen <= 0) {
            return ParseChapterResult.FileTooSmall(fileLen)
        }

        LogUtils.i(TAG, "Split chapters for file: $file")

        val lastModified = file.lastModified()

        try {
            RandomAccessFile(file, "r").use { bookStream ->
                val charset = bookStream.detectCharset()
                bookStream.seek(0)

                // 判断是否有符合预设的章节
                val chapterPattern = checkChapterType(bookStream, charset)
                if (chapterPattern == null) {
                    LogUtils.i(TAG, "No chapter pattern found. Try to slip chapter by lines.")
                } else {
                    LogUtils.i(TAG, "Chapter pattern found.")
                }

                var ioTime = 0L
                var newStringTime = 0L
                var encodeTime = 0L
                var patternTime = 0L
                var findMapTime = 0L
                var startTime: Long

                val byteCntBeforeMap = TreeMap<Int, Long>()

                when {
                    chapterPattern != null -> {
                        bookStream.seek(0L)
                        var chapterByteOffset = 0L
                        var lastPartByteCount = 0L
                        var firstChapter = true
                        var lastTitle = ""
                        var lastString = ""
                        var currentTitle: String
                        val reader = FileInputStream(bookStream.fd).bufferedReader(charset)

                        val buffer = CharArray(CHAR_BUFFER_SIZE)
                        var len: Int

                        startTime = SystemClock.elapsedRealtime()
                        while (reader.read(buffer).also { len = it } != -1) {
                            if (len <= 0) {
                                continue
                            }
                            byteCntBeforeMap.clear()
                            ioTime += SystemClock.elapsedRealtime() - startTime

                            startTime = SystemClock.elapsedRealtime()
                            val str = lastString + String(buffer, 0, len)
                            newStringTime += SystemClock.elapsedRealtime() - startTime

                            byteCntBeforeMap[0] = 0

                            startTime = SystemClock.elapsedRealtime()
                            val matcher = chapterPattern.matcher(str)
                            while (matcher.find()) {
                                patternTime += SystemClock.elapsedRealtime() - startTime
                                val start = matcher.start()
                                startTime = SystemClock.elapsedRealtime()

                                val byteCountBeforeChapter =
                                    when {
                                        start == 0 -> {
                                            0L
                                        }
                                        byteCntBeforeMap.containsKey(start) -> {
                                            byteCntBeforeMap[start]!!
                                        }
                                        else -> {
                                            startTime = SystemClock.elapsedRealtime()
                                            var index = 0
                                            var distance = start - 1
                                            for (key in byteCntBeforeMap.keys) {
                                                val curDis = abs(key - start)
                                                if (curDis < distance) {
                                                    index = key
                                                    distance = curDis
                                                }
                                            }
                                            findMapTime += SystemClock.elapsedRealtime() - startTime
                                            val subString = str.substring(
                                                min(start, index),
                                                max(start, index)
                                            )
                                            val size = subString.toByteArray(charset).size.toLong()
                                            val actualSize = if (index > start) {
                                                byteCntBeforeMap[index]!! - size
                                            } else {
                                                size + byteCntBeforeMap[index]!!
                                            }
                                            byteCntBeforeMap[start] = actualSize
                                            actualSize
                                        }
                                    }
                                encodeTime += SystemClock.elapsedRealtime() - startTime
                                currentTitle = lastTitle
                                lastTitle = matcher.group()

                                val fileOffset = byteCountBeforeChapter + lastPartByteCount
                                if (firstChapter && fileOffset > 0) {
                                    // 序章
                                    val chapter = BookChapter()
                                    chapter.title = "序章"
                                    chapter.start = 0
                                    chapter.end = fileOffset
                                    chapterByteOffset = fileOffset
                                    chapters.add(chapter)
                                    firstChapter = false
                                } else if (firstChapter) {
                                    // 没有序章
                                    // 跳过
                                    firstChapter = false
                                } else if (fileOffset > chapterByteOffset) {
                                    // 找到了新的一章
                                    val chapter = BookChapter()
                                    chapter.title = currentTitle
                                    chapter.start = chapterByteOffset
                                    chapter.end = fileOffset
                                    chapterByteOffset = fileOffset
                                    chapters.add(chapter)
                                }
                                startTime = SystemClock.elapsedRealtime()
                            }

                            // 有可能因为分段正好把一个章节分开了
                            // 这里要把没有分章的剩余字符串全部加到下一次迭代中
                            val last = byteCntBeforeMap.keys.last()
                            lastString = if (last != str.length) {
                                str.substring(last)
                            } else {
                                ""
                            }
                            lastPartByteCount += byteCntBeforeMap[last]!!

                            startTime = SystemClock.elapsedRealtime()
                        }

                        if (fileLen > chapterByteOffset) {
                            val chapter = BookChapter()
                            chapter.title = lastTitle
                            chapter.start = chapterByteOffset
                            chapter.end = fileLen
                            chapters.add(chapter)
                        }

                        chapters.forEachIndexed { index, bookChapter ->
                            bookChapter.order = index
                            bookChapter.lastModified = lastModified
                            bookChapter.bookId = bookFile.id
                            bookChapter.chapterType = BookChapter.ChapterType.REAL
                            if ("序章" != bookChapter.title) {
                                startTime = SystemClock.elapsedRealtime()
                                bookChapter.start += bookChapter.title.toByteArray(charset)
                                    .size.toLong()
                                encodeTime += SystemClock.elapsedRealtime() - startTime
                                bookChapter.title = bookChapter.title.trim()
                            }
                        }

                        LogUtils.d(
                            TAG,
                            "IO time=${ioTime}ms, patter time=${patternTime}ms, encodeTime=${encodeTime}ms, new String time=${newStringTime}, findMapTime=${findMapTime}}"
                        )
                    }

                    fileLen <= MAX_LENGTH_WITH_NO_CHAPTER -> {
                        LogUtils.i(
                            TAG,
                            "FileLen=${fileLen.toFileSizeString()} <= ${MAX_LENGTH_WITH_NO_CHAPTER.toFileSizeString()}. Use single chapter with filename as title."
                        )
                        chapters.add(
                            BookChapter(
                                null,
                                0,
                                lastModified,
                                bookFile.id,
                                bookFile.filename,
                                0L,
                                file.length(),
                                BookChapter.ChapterType.SINGLE
                            )
                        )
                    }

                    else -> {
                        LogUtils.i(
                            TAG,
                            "FileLen=${fileLen.toFileSizeString()} > ${MAX_LENGTH_WITH_NO_CHAPTER.toFileSizeString()}. Use virtual chapters."
                        )
                        bookStream.seek(0L)

                        // 不需要关闭，因为这个fd最终会被bookStream关掉的
                        val reader = ByteCountingLineReader(bookStream.fd, charset)
                        // 下一次需要分章的首个字节偏移
                        var chapterByteOffset = 0L
                        var lastLineByteCount: Long
                        var currentByteCount = 0L
                        var chapterIndex = 1

                        while (reader.readLine() != null) {
                            lastLineByteCount = currentByteCount
                            currentByteCount = reader.byteCount()

                            // 总计读入的内容超过了最大章节，就分一章
                            if (currentByteCount - chapterByteOffset > MAX_LENGTH_WITH_NO_CHAPTER) {
                                val chapter = BookChapter()
                                chapter.title = "第${chapterIndex++}章"
                                chapter.start = chapterByteOffset
                                chapter.end = lastLineByteCount
                                chapterByteOffset = lastLineByteCount
                                chapters.add(chapter)
                            }
                        }

                        // 最后一章
                        if (currentByteCount > chapterByteOffset + 1) {
                            val chapter = BookChapter()
                            chapter.title = "第${chapterIndex}章"
                            chapter.start = chapterByteOffset
                            chapter.end = currentByteCount
                            chapters.add(chapter)
                        }

                        LogUtils.d(TAG, "[Virtual chapters] fileLen=${fileLen}")
                        chapters.forEachIndexed { index, bookChapter ->
                            bookChapter.order = index
                            bookChapter.lastModified = lastModified
                            bookChapter.bookId = bookFile.id
                            // 这里也认为是真的章节
                            bookChapter.chapterType = BookChapter.ChapterType.VIRTUAL
                            LogUtils.d(
                                TAG,
                                "[Virtual chapters]：index=${index}, start=${bookChapter.start}, end=${bookChapter.end}"
                            )
                        }
                    }
                }

                LogUtils.i(TAG, "Chapter result: size=${chapters.size}")



                bookFile.charset = charset
                bookFile.handleChapterLastModified = lastModified
                val databaseTime = measureTimeMillis {
                    BookShelfOpenHelper.instance.saveBookChapters(bookFile, chapters)
                }
                LogUtils.d(TAG, "Database time=${databaseTime}")
            }

        } catch (e: IOException) {
            LogUtils.e(TAG, "Cannot open stream! File=${file}", e)
            return ParseChapterResult.FileOpenFail(e)
        }

        return ParseChapterResult.Success(chapters)
    }

    private fun checkChapterType(
        bookStream: RandomAccessFile,
        charset: Charset
    ): Pattern? {
        try {
            val buffer = ByteArray(BUFFER_SIZE)
            val length = bookStream.read(buffer, 0, buffer.size)
            if (length <= 0) {
                return null
            }
            for (str in CHAPTER_PATTERNS) {
                val pattern =
                    Pattern.compile(str, Pattern.MULTILINE)
                val matcher =
                    pattern.matcher(String(buffer, 0, length, charset))
                if (matcher.find()) {
                    return pattern
                }
            }
        } finally {
            bookStream.seek(0)
        }
        return null
    }
}
