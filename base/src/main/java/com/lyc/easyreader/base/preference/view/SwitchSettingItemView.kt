package com.lyc.easyreader.base.preference.view

import android.view.ContextThemeWrapper
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Switch
import com.lyc.easyreader.base.R
import com.lyc.easyreader.base.utils.dp2px

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
class SwitchSettingItemView(
    title: String = "",
    desc: String = ""
) : BaseSettingItemView(title, desc) {
    val switch = Switch(ContextThemeWrapper(context, R.style.SwitchTheme)).apply {
        showText = false
    }

    init {
        initView()
        setOnClickListener {
            switch.performClick()
        }
    }

    private fun initView() {
        addView(
            switch, LayoutParams(
                dp2px(56),
                WRAP_CONTENT
            )
        )
    }
}
