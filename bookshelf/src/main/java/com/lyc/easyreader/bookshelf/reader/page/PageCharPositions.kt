package com.lyc.easyreader.bookshelf.reader.page

/**
 * Created by Liu Yuchuan on 2020/2/29.
 */
data class PageCharPositions(
    @JvmField val chapter: Int,
    @JvmField val page: Int,
    @JvmField val contentWidth: Int,
    @JvmField val contentHeight: Int,
    @JvmField val indentString: String,
    @JvmField val contentTextSize: Float,
    @JvmField val lineSpaceFactor: Float,
    @JvmField val paramSpaceFactor: Float
) {
    @JvmField
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
