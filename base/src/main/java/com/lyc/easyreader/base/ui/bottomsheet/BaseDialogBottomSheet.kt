package com.lyc.easyreader.base.ui.bottomsheet

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.lyc.easyreader.base.R
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.ui.theme.NightModeManager

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
open class BaseDialogBottomSheet(context: Context) : Dialog(context) {
    private val rootView = FrameLayout(context)

    private fun applyNightMode() {
        if (!NightModeManager.nightModeEnable) {
            return
        }
        val maskView = FrameLayout(ReaderApplication.appContext()).apply {
            setBackgroundColor(NightModeManager.NIGHT_MODE_MASK_COLOR)
            this@BaseDialogBottomSheet.rootView.addView(this)
        }
        maskView.bringToFront()
        maskView.isVisible = true
    }

    init {
        initDialog()
    }

    private fun initDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        window?.run {
            decorView.setPadding(0)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.5f)
            val attributes = this.attributes
            attributes.width = ViewGroup.LayoutParams.MATCH_PARENT
            attributes.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes.gravity = Gravity.BOTTOM
            attributes.windowAnimations = R.style.BottomDialogAnim
            this.attributes = attributes
        }
    }

    override fun setContentView(layoutResID: Int) {
        layoutInflater.inflate(layoutResID, rootView, true)
        super.setContentView(rootView)
        applyNightMode()
    }

    override fun setContentView(view: View) {
        rootView.addView(view)
        super.setContentView(rootView)
        applyNightMode()
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        rootView.addView(view, params)
        super.setContentView(rootView, params)
        applyNightMode()
    }
}
