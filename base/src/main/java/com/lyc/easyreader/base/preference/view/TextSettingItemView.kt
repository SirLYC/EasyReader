package com.lyc.easyreader.base.preference.view

import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.lyc.easyreader.base.ui.theme.color_secondary_text
import com.lyc.easyreader.base.utils.buildCommonButtonTextColor
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.textSizeInDp

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
class TextSettingItemView(
    title: String = "",
    desc: String = "",
    contentTv: String = ""
) : BaseSettingItemView(title, desc) {
    val contentTv = TextView(context).apply {
        setTextColor(buildCommonButtonTextColor(color_secondary_text))
        textSizeInDp = 14f
        text = contentTv
        gravity = Gravity.CENTER
    }

    init {
        initView()
    }

    private fun initView() {
        addView(
            contentTv, LayoutParams(
                dp2px(56),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }
}
