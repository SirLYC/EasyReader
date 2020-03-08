package com.lyc.easyreader.bookshelf.secret.password

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.lifecycle.Observer
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.arch.provideViewModel
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.utils.vibrate
import com.lyc.easyreader.bookshelf.R
import com.lyc.easyreader.bookshelf.secret.SecretActivity
import com.lyc.easyreader.bookshelf.secret.SecretManager
import kotlinx.android.synthetic.main.activity_password.*

/**
 * Created by Liu Yuchuan on 2020/3/7.
 */
class PasswordActivity : BaseActivity(), View.OnClickListener {
    private var bookFile: BookFile? = null
    private lateinit var viewModel: PasswordViewModel

    companion object {
        private const val KEY_ACTION = "KEY_ACTION"
        private const val KEY_BOOK_FILES = "KEY_BOOK_FILES"

        fun openPasswordActivity(
            action: SecretManager.ActivityAction,
            bookFiles: Iterable<BookFile>? = null
        ) {
            val passwordAction = when (action) {
                SecretManager.ActivityAction.ModifyPassword -> SecretManager.PasswordAction.ModifyInput
                SecretManager.ActivityAction.OpenSecretPage -> if (SecretManager.hasPassword()) {
                    SecretManager.PasswordAction.Input
                } else {
                    SecretManager.PasswordAction.Set
                }
                SecretManager.ActivityAction.SetOrImportBookFile -> SecretManager.PasswordAction.Set
            }

            val context = ReaderApplication.appContext()
            val intent = Intent(context, PasswordActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(KEY_ACTION, SecretManager.actionToInt(passwordAction))
                if (bookFiles != null) {
                    putExtra(KEY_BOOK_FILES, bookFiles.toMutableList().toTypedArray())
                }
            }
            context.startActivity(intent)
        }
    }

    override fun beforeBaseOnCreate(savedInstanceState: Bundle?) {
        viewModel = provideViewModel()
        if (savedInstanceState == null) {
            val actionInt = intent?.getIntExtra(KEY_ACTION, -1) ?: -1
            if (actionInt == -1) {
                throw IllegalArgumentException("Cannot get valid action from $KEY_ACTION")
            }
            val action = SecretManager.intToAction(actionInt)
            val bookFiles =
                intent?.getParcelableArrayExtra(KEY_BOOK_FILES)?.mapNotNull { it as? BookFile }
                    ?.toTypedArray()
            viewModel.init(action, bookFiles)
        } else if (!isCreateFromConfigChange) {
            viewModel.restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState(outState)
    }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        if (!viewModel.isValid()) {
            ReaderToast.showToast("无法输入密码，请重试")
            return
        }
        LayoutInflater.from(this).inflate(R.layout.activity_password, rootView, true)

        val inputViews = layout_input_view.children.toList()
        val inputDrawable = getDrawable(R.drawable.password_input)
        val notInputDrawable = getDrawable(R.drawable.password_not_input)
        viewModel.currentInput.observe(this, Observer {
            val length = it.length
            inputViews.forEachIndexed { index, view ->
                if (index < length) {
                    view.background = inputDrawable
                } else {
                    view.background = notInputDrawable
                }
            }
        })

        bt_0.setOnClickListener(this)
        bt_1.setOnClickListener(this)
        bt_2.setOnClickListener(this)
        bt_3.setOnClickListener(this)
        bt_4.setOnClickListener(this)
        bt_5.setOnClickListener(this)
        bt_6.setOnClickListener(this)
        bt_7.setOnClickListener(this)
        bt_8.setOnClickListener(this)
        bt_9.setOnClickListener(this)

        viewModel.errorString.observe(this, Observer {
            if (it == null) {
                tv_error.text = " "
            } else {
                tv_error.text = it
            }
        })

        viewModel.currentAction.observeEvent(this, Observer {
            when (it) {
                SecretManager.PasswordAction.ActionBack -> {
                    onBackPressed()
                }

                SecretManager.PasswordAction.ActionImportOrOpen -> {
                    if (viewModel.importBookToSecret()) {
                        onBackPressed()
                    } else {
                        ReaderApplication.openActivity(SecretActivity::class)
                        finish()
                    }
                }
            }
        })

        viewModel.currentAction.observeState(this, Observer {
            tv_action.text = it.action
        })

        bt_backspace.setOnClickListener(this)
        bt_last_step.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        vibrate(50)
        when (v?.id) {
            R.id.bt_0 -> viewModel.input(0)
            R.id.bt_1 -> viewModel.input(1)
            R.id.bt_2 -> viewModel.input(2)
            R.id.bt_3 -> viewModel.input(3)
            R.id.bt_4 -> viewModel.input(4)
            R.id.bt_5 -> viewModel.input(5)
            R.id.bt_6 -> viewModel.input(6)
            R.id.bt_7 -> viewModel.input(7)
            R.id.bt_8 -> viewModel.input(8)
            R.id.bt_9 -> viewModel.input(9)
            R.id.bt_last_step -> if (!viewModel.handleLastStep()) {
                onBackPressed()
            }
            R.id.bt_backspace -> viewModel.backspace()
        }
    }
}
