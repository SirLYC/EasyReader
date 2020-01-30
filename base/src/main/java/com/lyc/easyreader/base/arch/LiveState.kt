package com.lyc.easyreader.base.arch

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
class LiveState<T>(initValue: T) {
    private val liveState = NonNullLiveData(initValue)
    private val liveEvent = SingleLiveEvent<T>().also { liveEvent ->
        liveState.observeForever { liveEvent.value = it }
    }

    var state: T
        @MainThread
        get() = liveState.value!!
        @MainThread
        set(value) {
            liveState.value = value
        }

    @AnyThread
    fun postState(value: T) {
        liveState.postValue(value)
    }

    fun observeEvent(owner: LifecycleOwner, observer: Observer<in T>) {
        liveEvent.observe(owner, observer)
    }

    fun observeState(owner: LifecycleOwner, observer: Observer<in T>) {
        liveEvent.observe(owner, observer)
    }

    fun observeEventForever(observer: Observer<in T>) {
        liveEvent.observeForever(observer)
    }

    fun observeStateForever(observer: Observer<in T>) {
        liveEvent.observeForever(observer)
    }
}
