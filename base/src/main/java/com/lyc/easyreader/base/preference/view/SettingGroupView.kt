package com.lyc.easyreader.base.preference.view

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.core.view.setPadding
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.ui.theme.color_orange
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf
import com.lyc.easyreader.base.utils.textSizeInPx

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
class SettingGroupView(groupName: String, color: Int = color_orange) :
    TextView(ReaderApplication.appContext()) {
    init {
        text = groupName
        setPadding(dp2px(16))
        setTextColor(color)
        textSizeInPx = dp2pxf(14f)
        paint.isFakeBoldText = true
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }
}
