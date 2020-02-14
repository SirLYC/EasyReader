package com.lyc.easyreader

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import com.lyc.easyreader.api.settings.ISettingGroup
import com.lyc.easyreader.api.settings.ISettings
import com.lyc.easyreader.base.getAppExtensions
import com.lyc.easyreader.base.preference.view.SettingGroupView
import com.lyc.easyreader.base.preference.view.SwitchSettingItemView
import com.lyc.easyreader.base.preference.view.TextSettingItemView
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.ui.theme.NightModeManager
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.base.utils.generateNewViewId
import com.lyc.easyreader.base.utils.showWithNightMode
import com.lyc.easyreader.base.utils.statusBarBlackText

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
class SettingActivity : BaseActivity(), View.OnClickListener {
    companion object {
        private val VIEW_ID_SCROLL_VIEW = generateNewViewId()
        private const val TAG = "SettingActivity"
        private val settings = getAppExtensions<ISettings>().apply {
            LogUtils.i(TAG, "RegisteredSettings: $this")
        }

        private val settingGroups = getAppExtensions<ISettingGroup>().sortedByDescending {
            it.priority()
        }.apply {
            LogUtils.i(TAG, "RegisteredSettingGroups: $this")
        }
    }

    private var settingsContainer: ViewGroup? = null

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        super.afterBaseOnCreate(savedInstanceState, rootView)
        val toolBar = BaseToolBar(this)
        toolBar.setTitle("设置")
        toolBar.setBarClickListener(this)
        rootView.addView(
            toolBar,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toolBar.getViewHeight())
        )

        val scrollView = NestedScrollView(this)
        scrollView.id = VIEW_ID_SCROLL_VIEW
        rootView.addView(
            scrollView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                topMargin = toolBar.getViewHeight()
            })
        val settingsContainer = LinearLayout(this).also { this.settingsContainer = it }
        scrollView.addView(
            settingsContainer,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        settingsContainer.orientation = LinearLayout.VERTICAL

        settingsContainer.addView(SettingGroupView("通用"))
        settingsContainer.addView(SwitchSettingItemView("夜间模式", "夜间模式会降低界面亮度并且在阅读界面使用夜间主题").apply {
            switch.setOnCheckedChangeListener { _, isChecked ->
                NightModeManager.nightMode.value = isChecked
            }
            NightModeManager.nightMode.observe(this@SettingActivity, Observer {
                switch.isChecked = it
            })
        })
        settingsContainer.addView(TextSettingItemView("恢复默认设置", "恢复包括阅读设置的所有设置").apply {
            drawDivider = false
            setOnClickListener {
                AlertDialog.Builder(this@SettingActivity)
                    .setMessage("确定恢复默认设置吗？")
                    .setPositiveButton("是") { _, _ ->
                        settings.forEach {
                            it.applyDefaultSettings()
                        }
                        ReaderToast.showToast("已恢复默认设置")
                    }
                    .setNegativeButton("否", null)
                    .showWithNightMode()
            }
        })
        settingGroups.forEach {
            it.attach(this)
            val list = it.createSettingViews()
            if (list.isNotEmpty()) {
                settingsContainer.addView(SettingGroupView(it.getGroupTitle()))
                list.forEach { settingView ->
                    settingsContainer.addView(settingView)
                }
            }
        }
    }

    override fun onResume() {
        window.statusBarBlackText(true)
        super.onResume()
    }

    override fun onDestroy() {
        settingGroups.forEach {
            it.detach(this)
            it.destroy()
        }
        // 防止内存泄露
        this.settingsContainer?.removeAllViews()
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            BaseToolBar.VIEW_ID_LEFT_BUTTON -> onBackPressed()
        }
    }
}
