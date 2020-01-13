package com.lyc.base.event

/**
 * Created by Liu Yuchuan on 2020/1/13.
 */
data class EventMessage @JvmOverloads constructor(
    val event: String,
    val obj: Any? = null,
    val arg1: Int = 0,
    val arg2: Int = 0
) {
    val time = System.currentTimeMillis()
}
