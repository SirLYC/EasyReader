package com.lyc.easyreader.usercenter

import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.InjectApiImpl
import com.lyc.easyreader.api.main.AbstractMainTabDelegate
import com.lyc.easyreader.api.main.IMainActivityDelegate
import com.lyc.easyreader.api.main.IMainTabDelegate

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@InjectApiImpl(api = IMainTabDelegate::class, createMethod = CreateMethod.GET_INSTANCE)
class UserCenterMainTabDelegate : AbstractMainTabDelegate<UserCenterFragment>(), IMainTabDelegate {
    companion object {
        @JvmStatic
        val instance by lazy { UserCenterMainTabDelegate() }
    }

    override fun getIconDrawableResId() = com.lyc.easyreader.api.R.drawable.ic_person_24dp
    override fun getOrder() = 10
    override fun getName() = "个人中心"
    override fun getId() = IMainActivityDelegate.ID_USER_CENTER
    override fun newFragmentInstance() = UserCenterFragment()
    override fun getFragmentClass() = UserCenterFragment::class.java
}
