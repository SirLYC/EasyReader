package com.lyc.easyreader.bookshelf.reader.settings

import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.preference.PreferenceManager

/**
 * Created by Liu Yuchuan on 2020/2/6.
 */
object ReaderSettings {
    private const val KEY = "reader_settings"
    private val preference = PreferenceManager.getPrefernce(KEY)

    private const val KEY_SCREEN_ORIENTATION = "screen_orientation"
    private const val KEY_FULL_SCREEN = "full_screen"

    val screenOrientation by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val value = preference.getString(KEY_SCREEN_ORIENTATION, "")?.let {
            try {
                ScreenOrientation.valueOf(it)
            } catch (e: Exception) {
                null
            }
        } ?: ScreenOrientation.PORTRAIT
        NonNullLiveData(value).apply {
            observeForever {
                preference.putString(KEY_SCREEN_ORIENTATION, it.name)
            }
        }
    }

    val fullscreen by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val value = preference.getBoolean(KEY_FULL_SCREEN, true)
        NonNullLiveData(value).apply {
            observeForever {
                preference.putBoolean(KEY_FULL_SCREEN, it)
            }
        }
    }
}
