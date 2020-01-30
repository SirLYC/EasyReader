package com.lyc.easyreader.base.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Created by Liu Yuchuan on 2020/1/8.
 */
abstract class BaseFragment : Fragment() {
    protected var rootView: View? = null

    val isActivityCreateFromConfigChange
        get() = (activity as? BaseActivity)?.isCreateFromConfigChange == true

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = newViewInstance(container)
        }
        if (savedInstanceState != null) {
            onRecoverState(savedInstanceState)
        }
        onViewReady(rootView)
        return rootView
    }

    open fun newViewInstance(container: ViewGroup?): View? = null

    open fun onViewReady(rootView: View?) = Unit

    open fun onRecoverState(savedInstanceState: Bundle) = Unit

    final override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        handleActivityResult(requestCode, resultCode, data)
    }

    open fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) = false

    open fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) = false
}
