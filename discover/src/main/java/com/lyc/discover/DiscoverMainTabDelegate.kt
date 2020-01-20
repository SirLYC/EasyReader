package com.lyc.discover

import com.lyc.api.main.AbstractMainTabDelegate
import com.lyc.api.main.IMainActivityDelegate
import com.lyc.api.main.IMainTabDelegate
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImpl

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@ExtensionImpl(extension = IMainTabDelegate::class, createMethod = CreateMethod.GET_INSTANCE)
class DiscoverMainTabDelegate : AbstractMainTabDelegate<DiscoverFragment>(), IMainTabDelegate {
    companion object {
        @JvmStatic
        val instance by lazy { DiscoverMainTabDelegate() }
    }

    override fun getIconDrawableResId() = com.lyc.api.R.drawable.ic_explore_24dp
    override fun getOrder() = 5
    override fun getName() = "发现"
    override fun getId() = IMainActivityDelegate.ID_DISCOVER
    override fun newFragmentInstance() = DiscoverFragment()
    override fun getFragmentClass() = DiscoverFragment::class.java
}
