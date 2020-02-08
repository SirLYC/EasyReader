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

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
abstract class BaseDialogFragment : DialogFragment() {
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
        NightModeManager.nightMode.observe(this, Observer {
            onNightModeChange(it)
        })
        this.dialog?.let { dialog ->
            dialog.window?.let {
                changeWindowAndDialogAttr(dialog, it)
            }
        }
        super.onActivityCreated(savedInstanceState)
    }

    open fun changeWindowAndDialogAttr(dialog: Dialog, window: Window) {

    }

    fun show(manager: FragmentManager): BaseDialogFragment {
        val fragment = manager.findFragmentByTag(showTag)
        val result = fragment as? BaseDialogFragment
        if (result != null) {
            return result
        }
        super.show(manager, showTag)
        return this
    }
}
