package com.lyc.easyreader.bookshelf.reader.settings

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import com.lyc.easyreader.base.preference.view.BaseSettingItemView
import com.lyc.easyreader.base.preference.view.SettingGroupView
import com.lyc.easyreader.base.preference.view.SwitchSettingItemView
import com.lyc.easyreader.base.preference.view.TextSettingItemView
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.utils.generateNewViewId
import com.lyc.easyreader.base.utils.showWithNightMode
import com.lyc.easyreader.base.utils.statusBarBlackText

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
class ReaderSettingsActivity : BaseActivity(), View.OnClickListener {
    companion object {
        private val VIEW_ID_SCROLL_VIEW = generateNewViewId()
    }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        super.afterBaseOnCreate(savedInstanceState, rootView)
        val topBar = BaseToolBar(this)
        topBar.setBarClickListener(this)
        topBar.setTitle("更多设置")
        rootView.addView(topBar, FrameLayout.LayoutParams(MATCH_PARENT, topBar.getViewHeight()))
        val nestedScrollView = NestedScrollView(this)
        // 配置改变时保留scrollPosition
        nestedScrollView.id = VIEW_ID_SCROLL_VIEW
        rootView.addView(
            nestedScrollView,
            FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                topMargin = topBar.getViewHeight()
            })
        val settingsContainer = LinearLayout(this)
        settingsContainer.orientation = LinearLayout.VERTICAL
        nestedScrollView.addView(settingsContainer)

        settingsContainer.addView(SettingGroupView("段落"))
        val settings = ReaderSettings.instance
        settingsContainer.addView(TextSettingItemView("行间距", "段内行与行之间的距离").apply {
            setOnClickListener {
                AlertDialog.Builder(this@ReaderSettingsActivity)
                    .setTitle("选择行间距")
                    .setItems(
                        LineSpaceFactor.values().map { it.displayName }.toTypedArray()
                    ) { _, index ->
                        settings.lineSpaceFactor.value = LineSpaceFactor.values()[index]
                    }.showWithNightMode()
            }
            settings.lineSpaceFactor.observe(this@ReaderSettingsActivity, Observer {
                contentTv.text = it.displayName
            })
        })

        settingsContainer.addView(TextSettingItemView("段间距", "段与段之间的距离").apply {
            setOnClickListener {
                AlertDialog.Builder(this@ReaderSettingsActivity)
                    .setTitle("选择段间距")
                    .setItems(
                        ParamSpaceFactor.values().map { it.displayName }.toTypedArray()
                    ) { _, index ->
                        settings.paraSpaceFactor.value = ParamSpaceFactor.values()[index]
                    }.showWithNightMode()
            }
            settings.paraSpaceFactor.observe(this@ReaderSettingsActivity, Observer {
                contentTv.text = it.displayName
            })
        })

        settingsContainer.addView(TextSettingItemView("段首缩进空格数", "用于段首缩进的空格字符数").apply {
            setOnClickListener {
                AlertDialog.Builder(this@ReaderSettingsActivity)
                    .setTitle("选择段首缩进空格数")
                    .setItems(
                        arrayOf("无", "1", "2", "4", "8")
                    ) { _, index ->
                        settings.indentCount.value = when (index) {
                            0 -> 0
                            else -> 1 shl (index - 1)
                        }
                    }.showWithNightMode()
            }
            settings.indentCount.observe(this@ReaderSettingsActivity, Observer {
                contentTv.text = it.toString()
            })
        })

        settingsContainer.addView(SwitchSettingItemView("段首缩进全角", "段首缩进使用全角字符").apply {
            drawDivider = false
            switch.setOnCheckedChangeListener { _, isChecked ->
                settings.indentFull.value = isChecked
            }
            settings.indentFull.observe(this@ReaderSettingsActivity, Observer {
                switch.isChecked = it
            })
        })

        settingsContainer.addView(SettingGroupView("阅读"))
        settingsContainer.addView(SwitchSettingItemView("音量键翻页", "“音量减”下一页，“音量加”上一页").apply {
            switch.setOnCheckedChangeListener { _, isChecked ->
                settings.volumeControlPage.value = isChecked
            }
            settings.volumeControlPage.observe(this@ReaderSettingsActivity, Observer {
                switch.isChecked = it
            })
        })
        settingsContainer.addView(TextSettingItemView("内容边距填充", "显示内容和边界的距离").apply {
            drawDivider = false
            setOnClickListener {
                AlertDialog.Builder(this@ReaderSettingsActivity)
                    .setTitle("选择内容边距填充")
                    .setItems(
                        ReaderMargin.values().map { it.displayName }.toTypedArray()
                    ) { _, index ->
                        settings.readerMargin.value = ReaderMargin.values()[index]
                    }.showWithNightMode()
            }
            settings.readerMargin.observe(this@ReaderSettingsActivity, Observer {
                contentTv.text = it.displayName
            })
        })


        settingsContainer.addView(SettingGroupView("其他"))
        settingsContainer.addView(BaseSettingItemView("恢复默认", "恢复所有阅读默认设置").apply {
            drawDivider = false
            setOnClickListener {
                AlertDialog.Builder(this@ReaderSettingsActivity)
                    .setMessage("要恢复阅读默认设置吗？")
                    .setPositiveButton("是") { _, _ ->
                        settings.applyDefaultSettings()
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
