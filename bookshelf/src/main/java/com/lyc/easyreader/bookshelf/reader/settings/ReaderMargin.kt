package com.lyc.easyreader.bookshelf.reader.settings

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
enum class ReaderMargin(
    val margin: Int,
    val displayName: String
) {
    NONE(0, "无"),
    SMALL(8, "小"),
    MEDIUM(16, "中"),
    LARGE(24, "大"),
}
