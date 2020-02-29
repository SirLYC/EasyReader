package com.lyc.easyreader.bookshelf.reader.settings

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
enum class LineSpaceFactor(
    val factor: Float,
    val displayName: String
) {
    SMALL(0f, "小"),
    MEDIUM(0.25f, "适中"),
    LARGE(0.6f, "大"),
    VERY_LARGE(1f, "非常大")
}
