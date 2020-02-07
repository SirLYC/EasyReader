package com.lyc.easyreader.bookshelf.reader.settings

import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImpl
import com.lyc.easyreader.api.settings.ISettings
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.preference.PreferenceManager
import com.lyc.easyreader.bookshelf.reader.page.PageLoader
import com.lyc.easyreader.bookshelf.reader.page.anim.PageAnimMode

/**
 * Created by Liu Yuchuan on 2020/2/6.
 */
@ExtensionImpl(extension = ISettings::class, createMethod = CreateMethod.GET_INSTANCE)
class ReaderSettings private constructor() : ISettings {
    companion object {
        private const val KEY = "reader_settings"

        private const val KEY_SCREEN_ORIENTATION = "screen_orientation"
        private val DEFAULT_SCREEN_ORIENTATION = ScreenOrientation.PORTRAIT

        private const val KEY_FULL_SCREEN = "full_screen"
        private const val DEFAULT_FULL_SCREEN = true

        private const val KEY_FONT_SIZE_IN_DP = "font_size_in_dp"
        private const val DEFAULT_FONT_SIZE_IN_DP = 16

        private const val KEY_PAGE_ANIM_MODE = "page_anim_mode"
        private val DEFAULT_PAGE_ANIM_MODE = PageAnimMode.SIMULATION

        private const val KEY_INDENT_COUNT = "indent_count"
        private const val DEFAULT_INDENT_COUNT = 2

        private const val KEY_INDENT_FULL = "indent_full"
        private const val DEFAULT_INDENT_FULL = true

        @JvmStatic
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { ReaderSettings() }
    }

    private val preference = PreferenceManager.getPrefernce(KEY)


    val screenOrientation by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val value = preference.getString(KEY_SCREEN_ORIENTATION)?.let {
            try {
                ScreenOrientation.valueOf(it)
            } catch (e: Exception) {
                null
            }
        } ?: DEFAULT_SCREEN_ORIENTATION
        NonNullLiveData(value).apply {
            observeForever {
                preference.putString(KEY_SCREEN_ORIENTATION, it.name)
            }
        }
    }

    val fullscreen by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val value = preference.getBoolean(KEY_FULL_SCREEN, DEFAULT_FULL_SCREEN)
        NonNullLiveData(value).apply {
            observeForever {
                preference.putBoolean(KEY_FULL_SCREEN, it)
            }
        }
    }

    val fontSizeInDp by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val value = preference.getInt(KEY_FONT_SIZE_IN_DP, DEFAULT_FONT_SIZE_IN_DP)
        NonNullLiveData(value).apply {
            observeForever {
                if (it > PageLoader.TEXT_SIZE_MAX_VALUE_DP) {
                    this.value = PageLoader.TEXT_SIZE_MAX_VALUE_DP
                    return@observeForever
                }

                if (it < PageLoader.TEXT_SIZE_MIN_VALUE_DP) {
                    this.value = PageLoader.TEXT_SIZE_MIN_VALUE_DP
                    return@observeForever
                }
                preference.putInt(KEY_FONT_SIZE_IN_DP, it)
            }
        }
    }

    val pageAnimMode by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val value = preference.getString(KEY_PAGE_ANIM_MODE)?.let {
            try {
                PageAnimMode.valueOf(it)
            } catch (e: Exception) {
                null
            }
        } ?: DEFAULT_PAGE_ANIM_MODE
        NonNullLiveData(value).apply {
            observeForever {
                preference.putString(KEY_PAGE_ANIM_MODE, it.name)
            }
        }
    }

    val indentCount by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val value = preference.getInt(KEY_INDENT_COUNT, DEFAULT_INDENT_COUNT)
        NonNullLiveData(value).apply {
            observeForever {
                if (it < 0) {
                    this.value = 0
                    return@observeForever
                }
                preference.putInt(KEY_INDENT_COUNT, it)
            }
        }
    }

    val indentFull by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val value = preference.getBoolean(KEY_INDENT_FULL, DEFAULT_INDENT_FULL)
        NonNullLiveData(value).apply {
            observeForever {
                preference.putBoolean(KEY_INDENT_FULL, it)
            }
        }
    }

    override fun applyDefaultSettings() {
        if (screenOrientation.value != DEFAULT_SCREEN_ORIENTATION) {
            screenOrientation.value = DEFAULT_SCREEN_ORIENTATION
        }

        if (fullscreen.value != DEFAULT_FULL_SCREEN) {
            fullscreen.value = DEFAULT_FULL_SCREEN
        }

        if (fontSizeInDp.value != DEFAULT_FONT_SIZE_IN_DP) {
            fontSizeInDp.value = DEFAULT_FONT_SIZE_IN_DP
        }
    }
}
