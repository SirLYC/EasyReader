package com.lyc.easyreader.bookshelf.reader

import android.view.View
import androidx.appcompat.app.AlertDialog
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImpl
import com.lyc.easyreader.api.settings.ISettingGroup
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.preference.view.TextSettingItemView
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.utils.showWithNightMode
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettingsActivity

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
@ExtensionImpl(extension = ISettingGroup::class, createMethod = CreateMethod.GET_INSTANCE)
object ReaderSettingGroup : ISettingGroup {
    @JvmStatic
    val instance = ReaderSettingGroup
    private var activity: BaseActivity? = null

    override fun attach(activity: BaseActivity) {
        ReaderSettingGroup.activity = activity
    }

    override fun getGroupTitle() = "阅读"

    override fun createSettingViews(): List<View> {
        val clearReadHistorySettingView = TextSettingItemView("清除阅读历史", "清除所有书架书籍的阅读记录")
        val otherReaderSettingView = TextSettingItemView("其他阅读设置", "部分阅读设置需要在阅读界面设置")

        otherReaderSettingView.setOnClickListener {
            ReaderApplication.openActivity(ReaderSettingsActivity::class)
        }
        clearReadHistorySettingView.setOnClickListener {
            activity?.run {
                AlertDialog.Builder(this)
                    .setMessage("确定清除所有阅读历史吗？")
                    .setPositiveButton("是") { _, _ ->
                        BookShelfOpenHelper.instance.deleteAllReadRecord {
                            ReaderToast.showToast("已清除")
                        }
                    }
                    .setNegativeButton("否", null)
                    .showWithNightMode()
            }
        }

        return listOf(
            clearReadHistorySettingView,
            otherReaderSettingView
        )
    }

    override fun detach(activity: BaseActivity) {
        ReaderSettingGroup.activity = null
    }

    override fun destroy() {

    }

    override fun priority() = 1
}
