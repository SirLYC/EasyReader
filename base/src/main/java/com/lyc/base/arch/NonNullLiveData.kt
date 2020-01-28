package com.lyc.base.arch

import androidx.lifecycle.MutableLiveData

/**
 * Created by Liu Yuchuan on 2020/1/26.
 */
class NonNullLiveData<T>(initValue: T) : MutableLiveData<T>() {
    init {
        value = initValue
    }

    override fun getValue(): T {
        return super.getValue()!!
    }
}
