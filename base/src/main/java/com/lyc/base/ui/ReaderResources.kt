package com.lyc.base.ui

import androidx.annotation.StringRes
import androidx.collection.LruCache
import com.lyc.base.ReaderApplication

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
object ReaderResources {
    private val stringResCache = object : LruCache<Int, String>(2 shl 20) {
        override fun sizeOf(key: Int, value: String) = value.length * 2
    }

    fun getString(@StringRes resId: Int): String {
        val cacheStr = stringResCache.get(resId)
        if (cacheStr != null) {
            return cacheStr
        }
        val string = ReaderApplication.appContext().resources.getString(resId)
        stringResCache.put(resId, string)
        return string
    }
}
