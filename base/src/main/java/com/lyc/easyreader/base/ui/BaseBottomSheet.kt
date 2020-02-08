package com.lyc.easyreader.base.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.view.WindowManager
import androidx.core.view.setPadding
import com.lyc.easyreader.base.R

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
abstract class BaseBottomSheet : BaseDialogFragment() {
    override fun changeWindowAndDialogAttr(dialog: Dialog, window: Window) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.decorView.setPadding(0)
        window.setDimAmount(0.5f)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        val attributes = window.attributes
        attributes.width = MATCH_PARENT
        attributes.height = WRAP_CONTENT
        attributes.gravity = Gravity.BOTTOM
        attributes.windowAnimations = R.style.BottomDialogAnim
        window.attributes = attributes
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.setOnShowListener {
            window.setWindowAnimations(0)
        }
    }
}
