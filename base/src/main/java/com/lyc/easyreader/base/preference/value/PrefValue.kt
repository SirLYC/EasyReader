package com.lyc.easyreader.base.preference.value

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.preference.IPreference

/**
 * Created by Liu Yuchuan on 2020/2/7.
 */
abstract class PrefValue<T>(
    private val key: String,
    val defaultValue: T,
    private val preference: IPreference,
    initValue: (key: String, defaultValue: T) -> T,
    persist: (key: String, value: T) -> Unit,
    private val validator: ((value: T) -> T)?,
    private val comparator: Comparator<T>?
) {
    private val liveData by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val value = initValue(
            key,
            defaultValue
        ).let { if (validator != null) validator.invoke(it) else it }
        NonNullLiveData(value).apply {
            observeForever {
                if (validator != null) {
                    val newVal = validator.invoke(it)
                    val equals = if (comparator != null) comparator.compare(
                        it,
                        newVal
                    ) == 0 else it == newVal
                    if (!equals) {
                        this.value = newVal
                        return@observeForever
                    }
                }
                persist(key, it)
            }
        }
    }

    var value
        get() = liveData.value
        set(value) {
            liveData.value = value
        }

    fun postValue(value: T) {
        liveData.postValue(value)
    }

    fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        liveData.observe(owner, observer)
    }

    fun observeForever(observer: Observer<T>) {
        liveData.observeForever(observer)
    }

    fun removeObserver(observer: Observer<T>) {
        liveData.removeObserver(observer)
    }

    fun removeObservers(owner: LifecycleOwner) {
        liveData.removeObservers(owner)
    }

    fun applyDefaultValue() {
        liveData.value = defaultValue
    }
}
