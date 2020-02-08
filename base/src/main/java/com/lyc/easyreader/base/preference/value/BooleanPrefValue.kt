package com.lyc.easyreader.base.preference.value

import com.lyc.easyreader.base.preference.IPreference

/**
 * Created by Liu Yuchuan on 2020/2/7.
 */
class BooleanPrefValue(
    key: String,
    defaultValue: Boolean,
    preference: IPreference
) : PrefValue<Boolean>(key, defaultValue, preference, { k, defVal ->
    preference.getBoolean(
        k,
        defVal
    )
}, { k, v -> preference.putBoolean(k, v) }, null, null) {
    fun flip() {
        this.value = !this.value
    }
}
