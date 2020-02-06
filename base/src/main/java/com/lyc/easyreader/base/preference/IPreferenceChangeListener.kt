package com.lyc.easyreader.base.preference

/**
 * Created by Liu Yuchuan on 2020/2/6.
 */
interface IPreferenceChangeListener {

    fun acceptPreference(pref: IPreference) = true

    fun onPreferenceChanged(pref: IPreference, key: String)
}
