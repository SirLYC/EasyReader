package com.lyc.easyreader.bookshelf

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.view.WindowManager.LayoutParams.*
import android.widget.EditText
import android.widget.FrameLayout
import com.lyc.easyreader.base.ui.BaseDialogFragment
import com.lyc.easyreader.base.utils.deviceWidth
import com.lyc.easyreader.bookshelf.scan.ScanSettings
import kotlinx.android.synthetic.main.layout_edit_filter_dialog.view.*
import kotlin.math.max

/**
 * Created by Liu Yuchuan on 2020/2/11.
 */
class ScanFilterEditDialog : BaseDialogFragment(), View.OnClickListener {

    private var editText: EditText? = null

    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_edit_filter_dialog, container, false)
        view.btn_ok.setOnClickListener(this)
        view.btn_cancel.setOnClickListener(this)
        editText = view.et
        view.et.setText(ScanSettings.filterSet.value.joinToString("\n"))
        view.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        return view
    }

    override fun changeWindowAndDialogAfterSetContent(dialog: Dialog, window: Window) {
        val lp = window.attributes
        lp.height = WRAP_CONTENT
        lp.width = (max(deviceWidth(), 360) * 0.9f).toInt()
        lp.dimAmount = 0.5f
        lp.flags =
            lp.flags or FLAG_DIM_BEHIND or FLAG_TRANSLUCENT_STATUS or FLAG_TRANSLUCENT_NAVIGATION
        window.attributes = lp
        dialog.setCanceledOnTouchOutside(false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_ok -> {
                if (submit()) {
                    dismiss()
                }
            }

            R.id.btn_cancel -> {
                dismiss()
            }
        }
    }

    private fun submit(): Boolean {
        val set = editText?.text?.toString()?.split("[\\n\\r]".toRegex())?.map {
            it.trim()
        }?.filter { !it.isBlank() }?.toSet() ?: setOf()
        ScanSettings.filterSet.value = set
        dismiss()
        return true
    }
}
