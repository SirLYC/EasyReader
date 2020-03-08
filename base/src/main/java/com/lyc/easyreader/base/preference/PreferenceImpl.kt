package com.lyc.easyreader.base.preference

import android.content.Context
import android.content.SharedPreferences
import com.lyc.easyreader.base.ReaderApplication

/**
 * Created by Liu Yuchuan on 2020/2/6.
 */
internal class PreferenceImpl(
    private val key: String
) : IPreference {

    companion object {
        inline fun SharedPreferences.writePref(func: (SharedPreferences.Editor) -> Unit) {
            val editor = edit()
            func(editor)
            editor.apply()
        }
    }

    val preference =
        ReaderApplication.appContext().getSharedPreferences(key, Context.MODE_PRIVATE)

    override fun getKey(): String {
        return key
    }

    override fun getString(key: String, defaultValue: String): String? {
        return preference.getString(key, defaultValue)
    }

    override fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? {
        return preference.getStringSet(key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preference.getBoolean(key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return preference.getInt(key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return preference.getLong(key, defaultValue)
    }

    override fun getFloat(key: String?, defaultValue: Float): Float {
        return preference.getFloat(key, defaultValue)
    }

    override fun putStringSet(key: String, value: Set<String>) {
        preference.writePref {
            it.putStringSet(key, value)
        }
    }

    override fun putString(key: String, value: String?) {
        preference.writePref {
            it.putString(key, value)
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        preference.writePref {
            it.putBoolean(key, value)
        }
    }

    override fun putInt(key: String, value: Int) {
        preference.writePref {
            it.putInt(key, value)
        }
    }

    override fun putLong(key: String, value: Long) {
        preference.writePref {
            it.putLong(key, value)
        }
    }

    override fun putFloat(key: String?, value: Float) {
        preference.writePref {
            it.putFloat(key, value)
        }
    }

    override fun clear() {
        preference.writePref {
            it.clear()
        }
    }
}
