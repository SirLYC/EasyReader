package com.lyc.easyreader.api.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.lyc.easyreader.base.ui.BaseFragment

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
abstract class AbstractMainTabDelegate<T : AbstractMainTabFragment> : IMainTabDelegate,
    Comparable<IMainTabDelegate> {
    protected var visible: Boolean? = null
    private var fragment: T? = null

    final override fun isVisible(): Boolean {
        return visible == true
    }

    final override fun onVisible() {
        if (visible != true) {
            visible = true
            fragment?.onVisible()
        }
    }

    final override fun onInvisible() {
        if (visible != false) {
            visible = false
            fragment?.onInvisible()
        }
    }

    abstract fun getFragmentClass(): Class<T>

    final override fun onThisTabClick() {
        fragment?.onThisTabClick()
    }

    override fun onBackPressed(): Boolean {
        return visible == true && fragment?.onBackPressed() == true
    }

    final override fun createFragment(): BaseFragment {
        val result = fragment ?: newFragmentInstance().also { fragment = it }
        result.arguments =
            Bundle().apply { putBoolean(IMainTabDelegate.KEY_VISIBLE, visible == true) }
        return result
    }

    final override fun recoverFragment(fragment: Fragment?) {
        if (this.fragment == fragment) {
            return
        }

        if (getFragmentClass().isInstance(fragment)) {
            @Suppress("UNCHECKED_CAST")
            this.fragment = (fragment as? T?)?.apply {
                visible = null
            }
        }
    }

    protected abstract fun newFragmentInstance(): T
}
