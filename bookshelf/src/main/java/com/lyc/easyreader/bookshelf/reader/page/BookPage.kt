package com.lyc.easyreader.bookshelf.reader.page

/**
 * Created by Liu Yuchuan on 2020/2/2.
 */
internal class BookPage {
    @JvmField
    var position = -1
    @JvmField
    var title: String? = null
    var charStart = -1
    var charEnd = -1
    //当前 lines 中为 title 的行数。
    @JvmField
    var titleLines = 0
    @JvmField
    var lines: List<String>? = null

    fun caulateCharCnt(): Int {
        if (lines == null) {
            return 0
        }

        var cnt = 0
        lines?.forEach {
            cnt += if (it.endsWith("\n")) {
                it.length - 1
            } else {
                it.length
            }
        }
        return cnt
    }
}
