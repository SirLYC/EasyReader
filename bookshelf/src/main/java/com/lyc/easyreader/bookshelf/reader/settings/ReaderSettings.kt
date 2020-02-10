package com.lyc.easyreader.bookshelf.reader.settings

import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImpl
import com.lyc.easyreader.api.settings.ISettings
import com.lyc.easyreader.base.preference.PreferenceManager
import com.lyc.easyreader.base.preference.value.BooleanPrefValue
import com.lyc.easyreader.base.preference.value.EnumPrefValue
import com.lyc.easyreader.base.preference.value.IntPrefValue
import com.lyc.easyreader.base.preference.value.PrefValue
import com.lyc.easyreader.base.ui.theme.NightModeManager
import com.lyc.easyreader.bookshelf.reader.page.PageLoader
import com.lyc.easyreader.bookshelf.reader.page.PageStyle
import com.lyc.easyreader.bookshelf.reader.page.anim.PageAnimMode

/**
 * Created by Liu Yuchuan on 2020/2/6.
 */
@ExtensionImpl(extension = ISettings::class, createMethod = CreateMethod.GET_INSTANCE)
class ReaderSettings private constructor() : ISettings {
    companion object {
        @JvmStatic
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { ReaderSettings() }

        val currentPageStyle
            get() = if (NightModeManager.nightModeEnable) PageStyle.NIGHT else instance.pageStyle.value
    }

    private val preference = PreferenceManager.getPrefernce("reader_settings")
    private val settingItems = mutableListOf<PrefValue<*>>()

    val screenOrientation = EnumPrefValue(
        "screen_orientation",
        ScreenOrientation.PORTRAIT,
        preference,
        { enumValueOf(it) }).also { settingItems.add(it) }

    val fullscreen = BooleanPrefValue("fullscreen", true, preference).also { settingItems.add(it) }

    val fontBold = BooleanPrefValue("font_bold", false, preference)

    val fontSizeInDp = IntPrefValue(
        "font_size_in_dp",
        16,
        preference,
        validator = {
            it.coerceIn(
                PageLoader.TEXT_SIZE_MIN_VALUE_DP,
                PageLoader.TEXT_SIZE_MAX_VALUE_DP
            )
        }).also { settingItems.add(it) }

    val pageAnimMode = EnumPrefValue(
        "page_anim_mode",
        PageAnimMode.SIMULATION,
        preference,
        { enumValueOf(it) }).also { settingItems.add(it) }

    val indentCount =
        IntPrefValue(
            "indent_count",
            2,
            preference,
            validator = { it.coerceAtLeast(0) }).also { settingItems.add(it) }

    val indentFull = BooleanPrefValue("indent_full", true, preference).also { settingItems.add(it) }

    val brightnessFollowSystem =
        BooleanPrefValue("brightness_follow_system", true, preference).also { settingItems.add(it) }

    val userBrightness =
        IntPrefValue(
            "user_brightness",
            0x7f,
            preference,
            validator = { it.coerceIn(0, 0xff) }).also { settingItems.add(it) }

    val keepScreenOn =
        BooleanPrefValue("keep_screen_on", true, preference).also { settingItems.add(it) }

    val lineSpaceFactor =
        EnumPrefValue(
            "line_space_factor",
            LineSpaceFactor.MEDIUM,
            preference,
            { enumValueOf(it) }).also { settingItems.add(it) }

    val paraSpaceFactor =
        EnumPrefValue(
            "para_space_factor",
            ParamSpaceFactor.MEDIUM,
            preference,
            { enumValueOf(it) }).also { settingItems.add(it) }

    val pageStyle = PageStylePrefValue("page_style", preference).also {
        settingItems.add(it)
    }

    val volumeControlPage =
        BooleanPrefValue("volume_control_page", false, preference).also { settingItems.add(it) }

    val readerMargin =
        EnumPrefValue(
            "reader_margin",
            ReaderMargin.MEDIUM,
            preference,
            { enumValueOf(it) }).also { settingItems.add(it) }

    override fun applyDefaultSettings() {
        settingItems.forEach {
            it.applyDefaultValue()
        }
    }
}
