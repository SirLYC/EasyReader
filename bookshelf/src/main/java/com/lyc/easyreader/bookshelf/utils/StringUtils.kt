package com.lyc.easyreader.bookshelf.utils

/**
 * Created by Liu Yuchuan on 2020/2/4.
 */
fun halfToFull(input: String): String? {
    val c = input.toCharArray()
    for (i in c.indices) {
        if (c[i] == 32.toChar()) //半角空格
        {
            c[i] = 12288.toChar()
            continue
        }
        if (c[i] > 32.toChar() && c[i] < 127.toChar()) //其他符号都转换为全角
            c[i] = (c[i] + 65248)
    }
    return String(c)
}
