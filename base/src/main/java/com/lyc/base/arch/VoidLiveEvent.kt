package com.lyc.base.arch

import androidx.annotation.MainThread

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class VoidLiveEvent : SingleLiveEvent<Void>() {
    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }
}
