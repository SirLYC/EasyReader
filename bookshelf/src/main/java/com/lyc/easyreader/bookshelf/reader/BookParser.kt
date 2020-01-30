package com.lyc.easyreader.bookshelf.reader

import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper
import com.lyc.easyreader.bookshelf.utils.detectCharset
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

        private const val MAX_LENGTH_WITH_NO_CHAPTER = 10 * 1024

        private const val BLANK_BYTE = 0x0a.toByte()

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
    }

    sealed class ParseChapterResult(
        val code: Int,
        val msg: String,
        val exception: Exception?,
        list: List<BookChapter>? = null
    ) {
        object FileNotExist : ParseChapterResult(CODE_FILE_NOT_EXIST, "文件不存在", null)
        class FileOpenFail(exception: Exception?) :
            ParseChapterResult(CODE_FILE_OPEN_FAILED, "文件打开失败", exception)

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

        val lastModified = file.lastModified()

        //获取文件流
        val bookStream = try {
            RandomAccessFile(file, "r")
        } catch (e: IOException) {
            LogUtils.e(TAG, "Cannot open stream! File=${file}", e)
            return ParseChapterResult.FileOpenFail(e)
        }

        val charset = bookStream.detectCharset()

        //寻找匹配文章标题的正则表达式，判断是否存在章节名
        val chapterPattern = checkChapterType(bookStream, charset)
        //加载章节
        val buffer = ByteArray(BUFFER_SIZE)
        //获取到的块起始点，在文件中的位置
        var curOffset: Long = 0
        //block的个数
        var blockPos = 0
        //读取的长度
        var readLen: Int
        //获取文件中的数据到buffer，直到没有数据为止
        while (bookStream.read(buffer, 0, buffer.size).also { readLen = it } > 0) {
            ++blockPos
            //如果存在Chapter
            if (chapterPattern != null) { //将数据转换成String
                val blockContent = String(buffer, 0, readLen, charset)
                //当前Block下使过的String的指针
                var seekPos = 0
                //进行正则匹配
                val matcher: Matcher = chapterPattern.matcher(blockContent)
                //如果存在相应章节
                while (matcher.find()) { //获取匹配到的字符在字符串中的起始位置
                    val chapterStart = matcher.start()
                    //如果 seekPos == 0 && nextChapterPos != 0 表示当前block处前面有一段内容
                    // 第一种情况一定是序章 第二种情况可能是上一个章节的内容
                    if (seekPos == 0 && chapterStart != 0) { //获取当前章节的内容
                        val chapterContent = blockContent.substring(seekPos, chapterStart)
                        //设置指针偏移
                        seekPos += chapterContent.length
                        //如果当前对整个文件的偏移位置为0的话，那么就是序章
                        if (curOffset == 0L) { //创建序章
                            val preChapter =
                                BookChapter()
                            preChapter.title = "序章"
                            preChapter.start = 0
                            preChapter.end = chapterContent.toByteArray(charset)
                                .size.toLong()//获取String的byte值,作为最终值
                            //如果序章大小大于30才添加进去
                            if (preChapter.end - preChapter.start > 30) {
                                chapters.add(preChapter)
                            }
                            //创建当前章节
                            val curChapter =
                                BookChapter()
                            curChapter.title = matcher.group()
                            curChapter.start = preChapter.end
                            chapters.add(curChapter)
                        } else {
                            //获取上一章节
                            val lastChapter: BookChapter = chapters[chapters.size - 1]
                            //将当前段落添加上一章去
                            lastChapter.end += chapterContent.toByteArray(charset).size.toLong()
                            //如果章节内容太小，则移除
                            if (lastChapter.end - lastChapter.start < 30) {
                                chapters.remove(lastChapter)
                            }
                            //创建当前章节
                            val curChapter =
                                BookChapter()
                            curChapter.title = matcher.group()
                            curChapter.start = lastChapter.end
                            chapters.add(curChapter)
                        }
                    } else { //是否存在章节
                        if (chapters.size != 0) { //获取章节内容
                            val chapterContent =
                                blockContent.substring(seekPos, matcher.start())
                            seekPos += chapterContent.length
                            //获取上一章节
                            val lastChapter: BookChapter = chapters[chapters.size - 1]
                            lastChapter.end =
                                lastChapter.start + chapterContent.toByteArray(charset).size.toLong()
                            //如果章节内容太小，则移除
                            if (lastChapter.end - lastChapter.start < 30) {
                                chapters.remove(lastChapter)
                            }
                            //创建当前章节
                            val curChapter =
                                BookChapter()
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
            } else {
                //章节在buffer的偏移量
                var chapterOffset = 0
                //当前剩余可分配的长度
                var strLength = readLen
                //分章的位置
                var chapterPos = 0
                while (strLength > 0) {
                    ++chapterPos
                    //是否长度超过一章
                    if (strLength > MAX_LENGTH_WITH_NO_CHAPTER) {
                        //在buffer中一章的终止点
                        var end = readLen
                        //寻找换行符作为终止点
                        for (i in chapterOffset + MAX_LENGTH_WITH_NO_CHAPTER until readLen) {
                            if (buffer[i] == BLANK_BYTE) {
                                end = i
                                break
                            }
                        }
                        val chapter =
                            BookChapter()
                        chapter.title = "第" + blockPos + "章" + "(" + chapterPos + ")"
                        chapter.start = curOffset + chapterOffset + 1
                        chapter.end = curOffset + end
                        chapters.add(chapter)
                        //减去已经被分配的长度
                        strLength -= (end - chapterOffset)
                        //设置偏移的位置
                        chapterOffset = end
                    } else {
                        val chapter =
                            BookChapter()
                        chapter.title = "第" + blockPos + "章" + "(" + chapterPos + ")"
                        chapter.start = curOffset + chapterOffset + 1
                        chapter.end = curOffset + readLen
                        chapters.add(chapter)
                        strLength = 0
                    }
                }
            }
            //block的偏移点
            curOffset += readLen.toLong()
            if (chapterPattern != null) { //设置上一章的结尾
                val lastChapter: BookChapter = chapters[chapters.size - 1]
                lastChapter.end = curOffset
            }
            //当添加的block太多的时候，执行GC
            if (blockPos % 15 == 0) {
                System.gc()
                System.runFinalization()
            }
        }

        chapters.forEachIndexed { index, bookChapter ->
            bookChapter.order = index
            bookChapter.lastModified = lastModified
        }

        BookShelfOpenHelper.instance.saveBookChapters(bookFile, lastModified, chapters)
        return ParseChapterResult.Success(chapters)
    }

    private fun checkChapterType(
        bookStream: RandomAccessFile,
        charset: Charset
    ): Pattern? {
        try {
            val buffer = ByteArray(BUFFER_SIZE)
            val length = bookStream.read(buffer, 0, buffer.size)
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
