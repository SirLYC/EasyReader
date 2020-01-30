package com.lyc.easyreader.discover

import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImpl
import com.lyc.easyreader.api.main.AbstractMainTabDelegate
import com.lyc.easyreader.api.main.IMainActivityDelegate
import com.lyc.easyreader.api.main.IMainTabDelegate

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@ExtensionImpl(extension = IMainTabDelegate::class, createMethod = CreateMethod.GET_INSTANCE)
class DiscoverMainTabDelegate : AbstractMainTabDelegate<DiscoverFragment>(), IMainTabDelegate {
    companion object {
        @JvmStatic
        val instance by lazy { DiscoverMainTabDelegate() }
    }

    override fun getIconDrawableResId() = com.lyc.easyreader.api.R.drawable.ic_explore_24dp
    override fun getOrder() = 5
    override fun getName() = "发现"
    override fun getId() = IMainActivityDelegate.ID_DISCOVER
    override fun newFragmentInstance() = DiscoverFragment()
    override fun getFragmentClass() = DiscoverFragment::class.java
}
