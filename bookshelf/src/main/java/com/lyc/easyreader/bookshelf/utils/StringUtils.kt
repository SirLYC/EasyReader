package com.lyc.easyreader.bookshelf.utils

import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.getOrSet

/**
 * Created by Liu Yuchuan on 2020/2/4.
 */
fun fullToHalf(input: String): String {
    val c = input.toCharArray()
    for (i in c.indices) {
        if (c[i] == 12288.toChar()) //全角空格
        {
            c[i] = 32.toChar()
            continue
        }
        if (c[i] > 65280.toChar() && c[i] < 65375.toChar()) c[i] = (c[i] - 65248)
    }
    return String(c)
}

fun halfToFull(input: String): String {
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

private val readerTimeFormatThreadLocal = ThreadLocal<SimpleDateFormat>()

fun Long.formatReaderTime(): String {
    return readerTimeFormatThreadLocal.getOrSet { SimpleDateFormat("HH:mm", Locale.ENGLISH) }
        .format(this)
}

fun Long.millisToString(): String {
    val minute = this / 60000
    if (minute <= 0) {
        return "不足1分钟"
    }

    if (minute < 60) {
        return "${minute}分钟"
    }

    return "%.1f小时".format(minute / 60f)
}
