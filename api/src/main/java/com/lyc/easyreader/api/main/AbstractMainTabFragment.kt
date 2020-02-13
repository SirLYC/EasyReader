package com.lyc.easyreader.api.main

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.core.view.isVisible
import com.lyc.easyreader.base.ui.BaseFragment

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
abstract class AbstractMainTabFragment : BaseFragment() {
    protected var visible: Boolean = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            visible = it.getBoolean(IMainTabDelegate.KEY_VISIBLE, false)
        }
    }

    override fun onViewReady(rootView: View?) {
        rootView?.isVisible = visible
    }

    @CallSuper
    open fun onVisible() {
        visible = true
        rootView?.isVisible = true
    }

    @CallSuper
    open fun onInvisible() {
        visible = false
        rootView?.isVisible = false
    }

    open fun onThisTabClick() {

    }

    open fun onBackPressed() = false
}
