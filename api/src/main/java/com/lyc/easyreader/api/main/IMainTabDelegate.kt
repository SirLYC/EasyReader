package com.lyc.easyreader.api.main

import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.lyc.appinject.annotations.Extension
import com.lyc.easyreader.base.ui.BaseFragment

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@Extension
interface IMainTabDelegate : Comparable<IMainTabDelegate> {

    companion object {
        const val KEY_VISIBLE = "KEY_VISIBLE"
    }

    fun getOrder(): Int

    fun getId(): Int

    fun getName(): String

    fun createFragment(): BaseFragment

    fun recoverFragment(fragment: Fragment?)

    fun isVisible(): Boolean

    fun onVisible()

    fun onInvisible()

    fun onThisTabClick()

    fun onBackPressed(): Boolean

    @DrawableRes
    fun getIconDrawableResId(): Int

    override fun compareTo(other: IMainTabDelegate) = this.getOrder() - other.getOrder()
}
