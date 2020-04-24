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
    private val liveEvent = SingleLiveEvent<T>().apply {
        liveState.observeForever { value = it }
    }

    var state: T
        @MainThread
        get() = liveState.value!!
        @MainThread
        set(value) {
            liveState.value = value
        }

    fun changeState(newVal: T): Boolean {
        if (state != newVal) {
            state = newVal
            return true
        }
        return false
    }

    @AnyThread
    fun postState(value: T) {
        liveState.postValue(value)
    }

    /**
     * 注册对状态变化的观察者
     */
    fun observeEvent(owner: LifecycleOwner, observer: Observer<in T>) {
        liveEvent.observe(owner, observer)
    }

    /**
     * 注册对状态的观察者
     */
    fun observeState(owner: LifecycleOwner, observer: Observer<in T>) {
        liveState.observe(owner, observer)
    }

    fun observeEventForever(observer: Observer<in T>) {
        liveEvent.observeForever(observer)
    }

    fun observeStateForever(observer: Observer<in T>) {
        liveState.observeForever(observer)
    }
}
