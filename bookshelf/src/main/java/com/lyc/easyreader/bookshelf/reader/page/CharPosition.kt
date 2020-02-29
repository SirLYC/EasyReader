package com.lyc.easyreader.bookshelf.reader.page

/**
 * Created by Liu Yuchuan on 2020/2/29.
 */
data class CharPosition(
    @JvmField
    val line: Int,
    @JvmField
    val charOffset: Int,
    @JvmField
    var x: Float,
    @JvmField
    var y: Float,
    @JvmField
    var width: Float,
    @JvmField
    var height: Float,
    @JvmField
    var drawHeight: Float
)
