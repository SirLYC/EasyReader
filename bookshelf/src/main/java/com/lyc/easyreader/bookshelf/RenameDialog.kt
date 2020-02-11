package com.lyc.easyreader.bookshelf

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager.LayoutParams.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputLayout
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.ui.BaseDialogFragment
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.utils.deviceWidth
import kotlinx.android.synthetic.main.layout_rename_dialog.*
import kotlinx.android.synthetic.main.layout_rename_dialog.view.*
import kotlin.math.max

/**
 * Created by Liu Yuchuan on 2020/2/11.
 */
class RenameDialog : BaseDialogFragment(), View.OnClickListener, TextWatcher,
    TextView.OnEditorActionListener {

    private var editText: EditText? = null
    private var editLayout: TextInputLayout? = null
    private var bookFile: BookFile? = null

    companion object {
        fun show(fm: FragmentManager, bookFile: BookFile) {
            val dialog = RenameDialog()
            dialog.arguments = Bundle().apply {
                putParcelable(KEY_FILE, bookFile)
            }
            dialog.showOneTag(fm)
        }

        const val KEY_FILE = "KEY_FILE"
    }

    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_rename_dialog, container, false)
        view.btn_ok.setOnClickListener(this)
        view.btn_cancel.setOnClickListener(this)
        editText = view.et
        view.et.setOnEditorActionListener(this)
        view.et.addTextChangedListener(this)
        editLayout = view.til
        bookFile = arguments?.getParcelable(KEY_FILE)
        bookFile?.let {
            view.et.setText(it.filename)
        }
        view.layoutParams =
            FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
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
        val text = editText?.text?.toString()?.replace("[\\t\\n\\r]".toRegex(), "")
            ?.replace("\\s+".toRegex(), " ")
        if (text == null || text.isBlank()) {
            til?.error = "输入不能为空"
            return false
        }

        if (text.length > 30) {
            til?.error = "最长30个字符"
            return false
        }

        val bookFile = bookFile
        if (bookFile == null) {
            ReaderToast.showToast("修改失败")
        } else {
            BookManager.instance.alterBookName(bookFile.id, text)
        }
        return true
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        til?.error = null
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if ((event == null || event.action == KeyEvent.ACTION_DOWN) && (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED)) {
            return if (submit()) {
                dismiss()
                false
            } else {
                true
            }
        }
        return true
    }
}
