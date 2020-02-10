package com.lyc.easyreader.bookshelf.reader.settings

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
enum class ParamSpaceFactor(
    val factor: Float,
    val displayName: String
) {
    NONE(0f, "无"),
    VERY_SMALL(0.25f, "小"),
    SMALL(0.5f, "较小"),
    MEDIUM(1f, "适中"),
    LARGE(2f, "较大"),
    VERY_LARGE(3f, "大")
}
