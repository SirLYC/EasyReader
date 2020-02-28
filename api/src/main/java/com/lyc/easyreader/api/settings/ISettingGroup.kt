package com.lyc.easyreader.api.settings

import android.view.View
import com.lyc.appinject.annotations.InjectApi
import com.lyc.easyreader.base.ui.BaseActivity

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
@InjectApi(oneToMany = true)
interface ISettingGroup {

    fun attach(activity: BaseActivity)

    fun getGroupTitle(): String

    fun createSettingViews(): List<View>

    fun detach(activity: BaseActivity)

    fun destroy()

    fun priority() = 0
}
