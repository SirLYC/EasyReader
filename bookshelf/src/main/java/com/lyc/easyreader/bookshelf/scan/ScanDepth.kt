package com.lyc.easyreader.bookshelf.scan

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
enum class ScanDepth(
    val depth: Int,
    val displayName: String = depth.toString(),
    val optionName: String = displayName
) {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
}
