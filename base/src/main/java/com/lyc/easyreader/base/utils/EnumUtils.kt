package com.lyc.easyreader.base.utils

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
inline fun <reified T : Enum<T>> T.nextValue(): T {
    val idx = ordinal
    val values = enumValues<T>()
    return values[(idx + 1) % values.size]
}
