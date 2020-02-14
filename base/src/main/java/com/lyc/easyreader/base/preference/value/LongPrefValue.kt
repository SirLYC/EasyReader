package com.lyc.easyreader.base.preference.value

import com.lyc.easyreader.base.preference.IPreference

/**
 * Created by Liu Yuchuan on 2020/2/7.
 */
class LongPrefValue(
    key: String,
    defaultValue: Long,
    preference: IPreference,
    validator: ((value: Long) -> Long)? = null
) : PrefValue<Long>(key, defaultValue, preference, { k, defVal ->
    preference.getLong(
        k,
        defVal
    )
}, { k, v ->
    preference.putLong(
        k,
        v
    )
}, validator, null) {
    fun inc() {
        value++
    }

    fun dec() {
        value--
    }
}
