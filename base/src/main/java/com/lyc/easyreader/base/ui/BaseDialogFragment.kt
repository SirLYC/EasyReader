package com.lyc.easyreader.base.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.ui.theme.NightModeManager
import com.lyc.easyreader.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
abstract class BaseDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "BaseDialogFragment"
    }

    private var maskView: View? = null
    private val showTag = javaClass.name

    private fun onNightModeChange(enable: Boolean) {
        val rootView = view as? FrameLayout ?: return

        if (!enable) {
            maskView?.isVisible = false
        } else {
            val maskView = this.maskView ?: FrameLayout(ReaderApplication.appContext()).apply {
                setBackgroundColor(NightModeManager.NIGHT_MODE_MASK_COLOR)
                rootView.addView(this)
                this@BaseDialogFragment.maskView = this
            }
            maskView.bringToFront()
            maskView.isVisible = true
        }
    }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = onCreateContentView(inflater, container, savedInstanceState)
        if (view != null) {
            val frameLayout = FrameLayout(ReaderApplication.appContext())
            frameLayout.addView(view)
            return frameLayout
        }

        return null
    }

    open fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = null

    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        this.dialog?.let { dialog ->
            dialog.window?.let {
                changeWindowAndDialogAttr(dialog, it)
            }
        }
        super.onActivityCreated(savedInstanceState)
        NightModeManager.nightMode.observe(this, Observer {
            onNightModeChange(it)
        })
        this.dialog?.let { dialog ->
            dialog.window?.let {
                changeWindowAndDialogAfterSetContent(dialog, it)
            }
        }
    }

    open fun changeWindowAndDialogAttr(dialog: Dialog, window: Window) {

    }


    open fun changeWindowAndDialogAfterSetContent(dialog: Dialog, window: Window) {

    }

    fun showOneTag(manager: FragmentManager, showTag: String?): BaseDialogFragment {
        LogUtils.d(TAG, "Show baseDialog, tag=${showTag}")
        val fragment = manager.findFragmentByTag(showTag)
        val result = fragment as? BaseDialogFragment
        if (result != null) {
            return result
        }
        super.show(manager, showTag)
        return this
    }

    open fun showOneTag(manager: FragmentManager): BaseDialogFragment {
        return showOneTag(manager, showTag)
    }
}
