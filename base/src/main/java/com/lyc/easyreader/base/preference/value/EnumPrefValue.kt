package com.lyc.easyreader.base.preference.value

import com.lyc.easyreader.base.preference.IPreference

/**
 * Created by Liu Yuchuan on 2020/2/7.
 */
class EnumPrefValue<T : Enum<T>>(
    key: String,
    defaultValue: T,
    preference: IPreference,
    valueOf: (String) -> T
) : PrefValue<T>(
    key,
    defaultValue,
    preference,
    { k, defVal ->
        preference.getString(k, defaultValue.name)?.let {
            try {
                valueOf(it)
            } catch (e: Exception) {
                null
            }
        } ?: defVal
    },
    { k, v -> preference.putString(k, v.name) },
    null,
    null
)
