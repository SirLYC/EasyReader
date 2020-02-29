package com.lyc.easyreader.bookshelf.reader.page

/**
 * Created by Liu Yuchuan on 2020/2/29.
 */
data class CharPosition(
    val line: Int,
    val charOffset: Int,
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float
)
