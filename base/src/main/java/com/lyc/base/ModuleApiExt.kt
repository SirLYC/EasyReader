package com.lyc.base

import com.lyc.appinject.ModuleApi

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
inline fun <reified T> getAppService(): T? {
    return ModuleApi.getInstance().getService(T::class.java)
}

inline fun <reified T> getAppExtensions(): List<T> {
    return ModuleApi.getInstance().getExtensions(T::class.java)
}
