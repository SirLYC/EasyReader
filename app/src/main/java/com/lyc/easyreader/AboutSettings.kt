package com.lyc.easyreader

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.lifecycle.Observer
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.InjectApiImpl
import com.lyc.easyreader.api.settings.ISettingGroup
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.preference.view.BaseSettingItemView
import com.lyc.easyreader.base.preference.view.SwitchSettingItemView
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/2/29.
 */
@InjectApiImpl(api = ISettingGroup::class, createMethod = CreateMethod.GET_INSTANCE)
object AboutSettings : ISettingGroup {
    @JvmStatic
    val instance = AboutSettings

    private var activity: BaseActivity? = null
    override fun attach(activity: BaseActivity) {
        this.activity = activity
    }

    override fun getGroupTitle() = "关于"

    override fun createSettingViews(): List<View> {
        return listOf(
            BaseSettingItemView("作者主页", "https://github.com/SirLYC (点击访问)").apply {
                setOnClickListener {
                    (ReaderApplication.appContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.run {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("https://github.com/SirLYC")
                        try {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ReaderApplication.appContext().startActivity(intent)
                        } catch (e: Throwable) {
                            LogUtils.e("AboutSettings", ex = e)
                        }
                    }
                }
            },
            BaseSettingItemView(
                "版本",
                "${BuildConfig.BUILD_TYPE.toUpperCase()}-${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}"
            ),
            SwitchSettingItemView("实验设置", "").apply {
                switch.setOnCheckedChangeListener { _, isChecked ->
                    ExperimentalSettings.show.value = isChecked
                }
                activity?.let { activity ->
                    ExperimentalSettings.show.observe(activity, Observer {
                        descTv.text = if (it) "已打开" else "已关闭"
                        switch.isChecked = it
                    })
                }
            }
        )
    }

    override fun detach(activity: BaseActivity) {
        this.activity = null
    }

    override fun destroy() {

    }

    override fun priority() = 0
}
