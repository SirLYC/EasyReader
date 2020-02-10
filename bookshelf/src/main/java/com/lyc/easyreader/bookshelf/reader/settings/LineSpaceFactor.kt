package com.lyc.easyreader.bookshelf.reader.settings

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
enum class LineSpaceFactor(
    val factor: Float,
    val displayName: String
) {
    NONE(0f, "无"),
    VERY_SMALL(0.1f, "小"),
    SMALL(0.3f, "较小"),
    MEDIUM(0.5f, "适中"),
    LARGE(1f, "较大"),
    VERY_LARGE(1.5f, "大")
}
