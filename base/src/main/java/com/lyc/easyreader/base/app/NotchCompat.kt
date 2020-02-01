package com.lyc.easyreader.base.app

import androidx.annotation.MainThread
import java.util.*

/**
 * Created by Liu Yuchuan on 2020/2/1.
 */
class NotchCompat private constructor() {
    var notchDevice = false
        internal set
    var notchDeviceConvinced = false
        internal set
    internal var postSetNotchDevice = false
    private val pendingCommands = LinkedList<(isNotchDevice: Boolean) -> Unit>()

    companion object {
        @JvmStatic
        val instance by lazy {
            NotchCompat()
        }
    }

    fun doOnIsNotchDeviceConvinced(func: (isNotchDevice: Boolean) -> Unit) {
        if (notchDeviceConvinced) {
            func(notchDevice)
        } else {
            pendingCommands.add(func)
        }
    }

    @MainThread
    internal fun runPendingCommands() {
        pendingCommands.forEach {
            it.invoke(notchDevice)
        }
        pendingCommands.clear()
    }
}
