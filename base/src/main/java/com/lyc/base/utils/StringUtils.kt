package com.lyc.base.utils

import java.security.MessageDigest

/**
 * Created by Liu Yuchuan on 2020/1/15.
 */
fun String.getMd5(): String {
    val instance = MessageDigest.getInstance("MD5")
    instance.update(this.toByteArray())
    val bytes = instance.digest()
    val sb = StringBuilder()
    bytes.forEach { byteVal ->
        val intVal = byteVal.toInt() and 0xff
        if (intVal < 16) {
            sb.append("0")
        }
        sb.append(intVal.toString(16))
    }
    return sb.toString()
}
