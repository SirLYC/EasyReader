package com.lyc.easyreader.bookshelf.reader.page

/**
 * Created by Liu Yuchuan on 2020/2/29.
 */
data class PageCharPositions(
    val chapter: Int,
    val page: Int,
    val contentWidth: Int,
    val contentHeight: Int,
    val indentString: String,
    val contentTextSize: Float,
    val lineSpaceFactor: Float,
    val paramSpaceFactor: Float
) {
    val positions = mutableListOf<CharPosition>()

    fun isCurrentPagePosition(
        chapter: Int,
        page: Int,
        contentWidth: Int,
        contentHeight: Int,
        indentString: String,
        contentTextSize: Float,
        lineSpaceFactor: Float,
        paramSpaceFactor: Float
    ): Boolean {
        return chapter == this.chapter && page == this.page && contentWidth == this.contentWidth && contentHeight == this.contentHeight && indentString == this.indentString && contentTextSize == this.contentTextSize && lineSpaceFactor == this.lineSpaceFactor && paramSpaceFactor == this.paramSpaceFactor
    }
}
