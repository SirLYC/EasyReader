package com.lyc.easyreader.base.preference

/**
 * Created by Liu Yuchuan on 2020/2/6.
 */
interface IPreference {
    fun getKey(): String

    fun getString(key: String, defaultValue: String = ""): String?

    fun getStringSet(key: String, defaultValue: Set<String>? = null): Set<String>?

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    fun getInt(key: String, defaultValue: Int = 0): Int

    fun getLong(key: String, defaultValue: Long = 0L): Long

    fun getFloat(key: String?, defaultValue: Float = 0f): Float

    fun putStringSet(key: String, value: Set<String>)

    fun putString(key: String, value: String?)

    fun putBoolean(key: String, value: Boolean)

    fun putInt(key: String, value: Int)

    fun putLong(key: String, value: Long)

    fun putFloat(key: String?, value: Float)

    fun clear()

}
