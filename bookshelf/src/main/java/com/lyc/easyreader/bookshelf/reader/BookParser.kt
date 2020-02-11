package com.lyc.easyreader.bookshelf.reader

import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper
import com.lyc.easyreader.bookshelf.utils.ByteCountingLineReader
import com.lyc.easyreader.bookshelf.utils.detectCharset
import com.lyc.easyreader.bookshelf.utils.toFileSizeString
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
class BookParser(private val bookFile: BookFile) {

    companion object {

        const val TAG = "BookParser"

        private const val BUFFER_SIZE = 512 * 1024

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
                val buffer = ByteArray(BUFFER_SIZE)
                // 当前buffer在文件中的起始偏移
                var curOffset: Long = 0
                // block的个数
                var blockCount = 0
                // 读取的长度
                var readLen: Int

                if (chapterPattern != null) {
                    while (bookStream.read(buffer, 0, buffer.size).also { readLen = it } > 0) {
                        ++blockCount
                        val blockContent = String(buffer, 0, readLen, charset)
                        var seekPos = 0
                        val matcher: Matcher = chapterPattern.matcher(blockContent)
                        while (matcher.find()) { //获取匹配到的字符在字符串中的起始位置
                            val chapterStart = matcher.start()
                            //如果 seekPos == 0 && nextChapterPos != 0 表示当前block处前面有一段内容
                            // 第一种情况一定是序章 第二种情况可能是上一个章节的内容
                            if (seekPos == 0 && chapterStart != 0) { //获取当前章节的内容
                                val chapterContent = blockContent.substring(seekPos, chapterStart)
                                //设置指针偏移
                                seekPos += chapterContent.length
                                //如果当前对整个文件的偏移位置为0的话，那么就是序章
                                if (curOffset == 0L) {
                                    // 序章
                                    val preChapter = BookChapter()
                                    preChapter.title = "序章"
                                    preChapter.start = 0
                                    preChapter.end =
                                        chapterContent.toByteArray(charset).size.toLong()
                                    if (preChapter.end - preChapter.start > 0) {
                                        chapters.add(preChapter)
                                    }
                                    val curChapter = BookChapter()
                                    curChapter.title = matcher.group()
                                    curChapter.start = preChapter.end
                                    chapters.add(curChapter)
                                } else {
                                    //获取上一章节
                                    val lastChapter: BookChapter = chapters[chapters.size - 1]
                                    //将当前段落添加上一章去
                                    lastChapter.end += chapterContent.toByteArray(charset)
                                        .size.toLong()
                                    //如果章节内容太小，则移除
                                    if (lastChapter.end - lastChapter.start <= 0) {
                                        chapters.remove(lastChapter)
                                    }
                                    //创建当前章节
                                    val curChapter = BookChapter()
                                    curChapter.title = matcher.group()
                                    curChapter.start = lastChapter.end
                                    chapters.add(curChapter)
                                }
                            } else {
                                if (chapters.size != 0) {
                                    //获取章节内容
                                    val chapterContent =
                                        blockContent.substring(seekPos, matcher.start())
                                    seekPos += chapterContent.length
                                    //获取上一章节
                                    val lastChapter: BookChapter = chapters[chapters.size - 1]
                                    lastChapter.end =
                                        lastChapter.start + chapterContent.toByteArray(charset).size.toLong()
                                    //如果章节内容太小，则移除
                                    if (lastChapter.end - lastChapter.start <= 0) {
                                        chapters.remove(lastChapter)
                                    }
                                    //创建当前章节
                                    val curChapter = BookChapter()
                                    curChapter.title = matcher.group()
                                    curChapter.start = lastChapter.end
                                    chapters.add(curChapter)
                                } else {
                                    val curChapter =
                                        BookChapter()
                                    curChapter.title = matcher.group()
                                    curChapter.start = 0
                                    chapters.add(curChapter)
                                }
                            }
                        }
                        curOffset += readLen.toLong()
                        val lastChapter: BookChapter = chapters[chapters.size - 1]
                        lastChapter.end = curOffset
                    }
                    chapters.forEachIndexed { index, bookChapter ->
                        bookChapter.order = index
                        bookChapter.lastModified = lastModified
                        bookChapter.bookId = bookFile.id
                        bookChapter.chapterType = BookChapter.ChapterType.REAL
                        if ("序章" != bookChapter.title) {
                            bookChapter.start += bookChapter.title.toByteArray(charset)
                                .size.toLong()
                            bookChapter.title = bookChapter.title.trim()
                        }
                    }
                } else if (fileLen <= MAX_LENGTH_WITH_NO_CHAPTER) {
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
                } else {
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

                LogUtils.i(TAG, "Chapter result: size=${chapters.size}")



                bookFile.charset = charset
                bookFile.handleChapterLastModified = lastModified

                BookShelfOpenHelper.instance.saveBookChapters(bookFile, chapters)
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
