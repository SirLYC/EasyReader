package com.lyc.easyreader.bookshelf.reader.settings

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
enum class ParamSpaceFactor(
    val factor: Float,
    val displayName: String
) {
    SMALL(0f, "小"),
    MEDIUM(0.5f, "适中"),
    LARGE(1f, "大"),
    VERY_LARGE(2f, "非常大")
}
