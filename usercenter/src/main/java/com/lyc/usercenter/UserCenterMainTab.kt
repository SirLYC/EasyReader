package com.lyc.usercenter

import com.lyc.api.main.AbstractMainTab
import com.lyc.api.main.IMainActivityEventBus
import com.lyc.api.main.IMainTab
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImpl

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@ExtensionImpl(extension = IMainTab::class, createMethod = CreateMethod.GET_INSTANCE)
class UserCenterMainTab : AbstractMainTab<UserCenterFragment>(), IMainTab {
    companion object {
        @JvmStatic
        val instance by lazy { UserCenterMainTab() }
    }

    override fun getId() = IMainActivityEventBus.ID_USER_CENTER
    override fun newFragmentInstance() = UserCenterFragment()
    override fun getFragmentClass() = UserCenterFragment::class.java
}
