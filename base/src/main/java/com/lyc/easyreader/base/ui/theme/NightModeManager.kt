package com.lyc.easyreader.base.ui.theme

import com.lyc.easyreader.base.preference.PreferenceManager
import com.lyc.easyreader.base.preference.value.BooleanPrefValue

/**
 * Created by Liu Yuchuan on 2020/1/19.
 */
object NightModeManager {

    const val NIGHT_MODE_MASK_COLOR = 0x7F000000

    val nightModeEnable
        get() = nightMode.value

    private val preference = PreferenceManager.getPrefernce("night_mode")

    val nightMode = BooleanPrefValue("night_mode", false, preference)
}
