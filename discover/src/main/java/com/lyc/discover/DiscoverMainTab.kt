package com.lyc.discover

import com.lyc.api.main.AbstractMainTab
import com.lyc.api.main.IMainActivityEventBus
import com.lyc.api.main.IMainTab
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImpl

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@ExtensionImpl(extension = IMainTab::class, createMethod = CreateMethod.GET_INSTANCE)
class DiscoverMainTab : AbstractMainTab<DiscoverFragment>(), IMainTab {
    companion object {
        @JvmStatic
        val instance by lazy { DiscoverMainTab() }
    }

    override fun getId() = IMainActivityEventBus.ID_DISCOVER
    override fun newFragmentInstance() = DiscoverFragment()
    override fun getFragmentClass() = DiscoverFragment::class.java
}
