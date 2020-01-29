package com.lyc.base.ui.theme

import com.lyc.common.EventHubFactory

/**
 * Created by Liu Yuchuan on 2020/1/19.
 */
object NightModeManager {

    const val NIGHT_MODE_MASK_COLOR = 0x7F000000

    private val eventHub = EventHubFactory.createDefault<INightModeChangeListener>(true)
    var nightModeEnable = false
        set(value) {
            if (value != field) {
                field = value
                eventHub.getEventListeners().forEach {
                    it.onNightModeChange(value)
                }
            }
        }

    fun addNightModeChangeListener(listener: INightModeChangeListener) {
        eventHub.addEventListener(listener)
    }

    fun removeNightModeChangeListener(listener: INightModeChangeListener) {
        eventHub.removeEventListener(listener)
    }

    interface INightModeChangeListener {
        fun onNightModeChange(enable: Boolean)
    }
}
