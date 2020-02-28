package com.lyc.easyreader.bookshelf.scan

import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.InjectApiImpl
import com.lyc.easyreader.api.settings.ISettings
import com.lyc.easyreader.base.preference.PreferenceManager
import com.lyc.easyreader.base.preference.value.BooleanPrefValue
import com.lyc.easyreader.base.preference.value.EnumPrefValue
import com.lyc.easyreader.base.preference.value.PrefValue
import com.lyc.easyreader.base.preference.value.StringSetPreValue
import com.lyc.easyreader.bookshelf.utils.fullToHalf

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
@InjectApiImpl(api = ISettings::class, createMethod = CreateMethod.GET_INSTANCE)
object ScanSettings : ISettings {
    @JvmStatic
    val instance = ScanSettings

    private val preference = PreferenceManager.getPrefernce("scan_settings")
    private val settingItems = mutableListOf<PrefValue<*>>()

    val scanDepth = EnumPrefValue(
        "scan_depth",
        ScanDepth.THREE,
        preference,
        { enumValueOf(it) }).also { settingItems.add(it) }

    val scanInvisibleFile = BooleanPrefValue(
        "scan_invisible_file",
        false,
        preference
    ).also { settingItems.add(it) }

    val enableFilter = BooleanPrefValue(
        "enable_filter",
        true,
        preference
    ).also { settingItems.add(it) }

    val filterSet = StringSetPreValue(
        "filter_set",
        setOf(
            "debug",
            "log",
            "test",
            "cache",
            "crash",
            "guid",
            "sys",
            "stat",
            "com.",
            "org.",
            "info",
            "config",
            "device",
            "uuid",
            "template"
        ),
        preference
    ) { set ->
        var ok = true
        for (value in set) {
            if (fullToHalf(value).isBlank()) {
                ok = false
                break
            }
        }
        if (ok) set else set.filter { !fullToHalf(it).isBlank() }.toSet()
    }.also { settingItems.add(it) }

    override fun applyDefaultSettings() {
        settingItems.forEach {
            it.applyDefaultValue()
        }
    }
}
