package com.lyc.easyreader.bookshelf.reader.page

/**
 * Created by Liu Yuchuan on 2020/2/2.
 */
internal class BookPage {
    @JvmField
    var position = 0
    @JvmField
    var title: String? = null
    //当前 lines 中为 title 的行数。
    @JvmField
    var titleLines = 0
    @JvmField
    var lines: List<String>? = null
}
