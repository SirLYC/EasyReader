package com.lyc.api.main

import androidx.fragment.app.Fragment
import com.lyc.appinject.annotations.Extension
import com.lyc.base.ui.BaseFragment

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@Extension
interface IMainTab {

    companion object {
        const val KEY_VISIBLE = "KEY_VISIBLE"
    }

    fun getId(): Int

    fun createFragment(): BaseFragment

    fun recoverFragment(fragment: Fragment?)

    fun isVisible(): Boolean

    fun onVisible()

    fun onInvisible()

    fun onThisTabClick()
}
