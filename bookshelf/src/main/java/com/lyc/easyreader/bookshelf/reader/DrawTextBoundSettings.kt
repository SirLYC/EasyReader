package com.lyc.easyreader.bookshelf.reader

import android.view.View
import androidx.lifecycle.Observer
import com.lyc.appinject.annotations.InjectApiImpl
import com.lyc.easyreader.api.settings.IExperimentalSettingItem
import com.lyc.easyreader.base.preference.view.SwitchSettingItemView
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettings

/**
 * Created by Liu Yuchuan on 2020/2/29.
 */
@InjectApiImpl(api = IExperimentalSettingItem::class)
class DrawTextBoundSettings : IExperimentalSettingItem {
    private var activity: BaseActivity? = null
    override fun attach(activity: BaseActivity) {
        this.activity = activity
    }

    override fun createSettingItemView(): View {
        return SwitchSettingItemView("小说文字绘制边界", "").apply {
            switch.setOnCheckedChangeListener { _, isChecked ->
                ReaderSettings.instance.drawTextBound.value = isChecked
            }
            activity?.let { activity ->
                ReaderSettings.instance.drawTextBound.observe(activity, Observer {
                    descTv.text = if (it) "已打开" else "已关闭"
                    switch.isChecked = it
                })
            }
        }

    }

    override fun detach(activity: BaseActivity) {
        this.activity = null
    }

    override fun destroy() {

    }
}
