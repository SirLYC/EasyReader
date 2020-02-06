package com.lyc.easyreader.base.app

import androidx.annotation.MainThread
import java.util.*

/**
 * Created by Liu Yuchuan on 2020/2/1.
 */
class NotchCompat private constructor() {
    var notchDevice = false
        internal set
    var notchInfoConvinced = false
        internal set
    var notchHeight: Int = 0
    internal var postSetNotchDevice = false
    private val pendingCommands = LinkedList<(isNotchDevice: Boolean, notchHeight: Int) -> Unit>()

    companion object {
        @JvmStatic
        val instance by lazy {
            NotchCompat()
        }
    }

    fun doOnIsNotchInfoConvinced(func: (isNotchDevice: Boolean, notchHeight: Int) -> Unit) {
        if (notchInfoConvinced) {
            func(notchDevice, notchHeight)
        } else {
            pendingCommands.add(func)
        }
    }

    @MainThread
    internal fun runPendingCommands() {
        pendingCommands.forEach {
            it.invoke(notchDevice, notchHeight)
        }
        pendingCommands.clear()
    }
}
