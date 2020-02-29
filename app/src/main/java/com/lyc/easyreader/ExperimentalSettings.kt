package com.lyc.easyreader

import android.view.View
import com.lyc.easyreader.api.settings.IExperimentalSettingItem
import com.lyc.easyreader.api.settings.ISettingGroup
import com.lyc.easyreader.base.getOneToManyApiList
import com.lyc.easyreader.base.preference.PreferenceManager
import com.lyc.easyreader.base.preference.value.BooleanPrefValue
import com.lyc.easyreader.base.ui.BaseActivity

/**
 * Created by Liu Yuchuan on 2020/2/29.
 */
object ExperimentalSettings : ISettingGroup {
    private val preference = PreferenceManager.getPrefernce("experimental_settings")

    val show = BooleanPrefValue("show", false, preference)

    val settings by lazy {
        getOneToManyApiList<IExperimentalSettingItem>().sortedBy { it.priority() }
    }

    override fun attach(activity: BaseActivity) {
        settings.forEach { it.attach(activity) }
    }

    override fun getGroupTitle() = "实验特性"

    override fun createSettingViews(): List<View> {
        return settings.map { it.createSettingItemView() }
    }

    override fun detach(activity: BaseActivity) {
        settings.forEach { it.detach(activity) }
    }

    override fun destroy() {
        settings.forEach { it.destroy() }
    }

    override fun priority() = -1
}
