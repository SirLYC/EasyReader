package com.lyc.easyreader.bookshelf.scan

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.InjectApiImpl
import com.lyc.easyreader.api.settings.ISettingGroup
import com.lyc.easyreader.base.preference.view.BaseSettingItemView
import com.lyc.easyreader.base.preference.view.SwitchSettingItemView
import com.lyc.easyreader.base.preference.view.TextSettingItemView
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.utils.showWithNightMode
import com.lyc.easyreader.bookshelf.ScanFilterEditDialog

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
@InjectApiImpl(api = ISettingGroup::class, createMethod = CreateMethod.GET_INSTANCE)
object ScanSettingGroup : ISettingGroup {
    @JvmStatic
    val instance = ScanSettingGroup
    private var activity: BaseActivity? = null

    override fun attach(activity: BaseActivity) {
        this.activity = activity
    }

    override fun getGroupTitle() = "扫描文件夹"

    override fun createSettingViews(): List<View> {
        val scanDepthSettingView = TextSettingItemView("扫描深度", "扫描文件夹时的扫描深度，过大可能会导致扫描慢").apply {
            setOnClickListener {
                activity?.run {
                    AlertDialog.Builder(this)
                        .setItems(
                            ScanDepth.values().map { it.optionName }.toTypedArray()
                        ) { _, index ->
                            ScanSettings.scanDepth.value = ScanDepth.values()[index]
                        }.showWithNightMode()
                }
            }
            activity?.let {
                ScanSettings.scanDepth.observe(it, Observer { depth ->
                    contentTv.text = depth.displayName
                })
            }
        }

        val scanInvisibleFileSettingItemView = SwitchSettingItemView("扫描隐藏文件", "").apply {
            switch.setOnCheckedChangeListener { _, isChecked ->
                ScanSettings.scanInvisibleFile.value = isChecked
            }
            activity?.let { activity ->
                ScanSettings.scanInvisibleFile.observe(activity, Observer {
                    switch.isChecked = it
                    descTv.text = if (it) "开启" else "关闭"
                })
            }
        }

        val enableFilterSettingItemView = SwitchSettingItemView("关键字过滤", "扫描时对文件名进行过滤").apply {
            switch.setOnCheckedChangeListener { _, isChecked ->
                ScanSettings.enableFilter.value = isChecked
            }
            activity?.let { activity ->
                ScanSettings.enableFilter.observe(activity, Observer {
                    switch.isChecked = it
                    drawDivider = it
                })
            }
        }

        val editFilterSettingItemView = BaseSettingItemView("编辑过滤关键字", "过滤的关键字，不区分大小写").apply {
            drawDivider = false
            activity?.let { activity ->
                ScanSettings.enableFilter.observe(activity, Observer {
                    isVisible = it
                })
            }
            setOnClickListener {
                activity?.run {
                    ScanFilterEditDialog().showOneTag(supportFragmentManager)
                }
            }
        }

        return listOf(
            scanDepthSettingView,
            scanInvisibleFileSettingItemView,
            enableFilterSettingItemView,
            editFilterSettingItemView
        )
    }

    override fun detach(activity: BaseActivity) {
        this.activity = null
    }

    override fun destroy() {

    }
}
