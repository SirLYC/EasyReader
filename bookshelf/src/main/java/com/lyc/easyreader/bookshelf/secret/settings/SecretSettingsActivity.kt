package com.lyc.easyreader.bookshelf.secret.settings

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import com.lyc.easyreader.base.preference.view.BaseSettingItemView
import com.lyc.easyreader.base.preference.view.SettingGroupView
import com.lyc.easyreader.base.preference.view.TextSettingItemView
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.utils.generateNewViewId
import com.lyc.easyreader.base.utils.showWithNightMode
import com.lyc.easyreader.base.utils.statusBarBlackText
import com.lyc.easyreader.bookshelf.secret.SecretManager
import com.lyc.easyreader.bookshelf.secret.password.PasswordActivity

/**
 * Created by Liu Yuchuan on 2020/3/9.
 */
class SecretSettingsActivity : BaseActivity(), View.OnClickListener {
    companion object {
        private val VIEW_ID_SCROLL_VIEW = generateNewViewId()
    }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        super.afterBaseOnCreate(savedInstanceState, rootView)
        val topBar = BaseToolBar(this)
        topBar.setBarClickListener(this)
        topBar.setTitle("私密空间设置")
        rootView.addView(
            topBar,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, topBar.getViewHeight())
        )
        val nestedScrollView = NestedScrollView(this)
        // 配置改变时保留scrollPosition
        nestedScrollView.id = VIEW_ID_SCROLL_VIEW
        rootView.addView(
            nestedScrollView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                topMargin = topBar.getViewHeight()
            })
        val settingsContainer = LinearLayout(this)
        settingsContainer.orientation = LinearLayout.VERTICAL
        nestedScrollView.addView(settingsContainer)

        settingsContainer.addView(SettingGroupView("密码"))
        settingsContainer.addView(BaseSettingItemView("修改密码", "修改私密空间的密码").apply {
            setOnClickListener {
                PasswordActivity.openPasswordActivity(SecretManager.ActivityAction.ModifyPassword)
            }
        })

        settingsContainer.addView(TextSettingItemView("密码有效时间", "输入密码后在时效内可以不输入密码进入私密空间").apply {
            setOnClickListener {
                AlertDialog.Builder(this@SecretSettingsActivity)
                    .setItems(
                        PasswordSession.values().map { it.displayName }.toTypedArray()
                    ) { _, index ->
                        SecretManager.passwordSession.value = PasswordSession.values()[index]
                    }.showWithNightMode()
            }
            SecretManager.passwordSession.observe(this@SecretSettingsActivity, Observer {
                contentTv.text = it.displayName
            })
        })


        settingsContainer.addView(SettingGroupView("其他"))
        settingsContainer.addView(BaseSettingItemView("恢复默认", "恢复私密默认设置（不包括密码）").apply {
            drawDivider = false
            setOnClickListener {
                AlertDialog.Builder(this@SecretSettingsActivity)
                    .setMessage("要恢复私密默认设置吗？")
                    .setPositiveButton("是") { _, _ ->
                        SecretManager.secretSettings.forEach {
                            it.applyDefaultValue()
                        }
                        ReaderToast.showToast("已恢复默认设置")
                    }
                    .setNegativeButton("否", null)
                    .showWithNightMode()
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            BaseToolBar.VIEW_ID_LEFT_BUTTON -> {
                onBackPressed()
            }
        }
    }

    override fun onResume() {
        window.statusBarBlackText(true)
        super.onResume()
    }
}
