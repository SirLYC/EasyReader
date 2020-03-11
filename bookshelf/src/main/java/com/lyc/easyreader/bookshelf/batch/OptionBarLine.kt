package com.lyc.easyreader.bookshelf.batch

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf

/**
 * Created by Liu Yuchuan on 2020/3/4.
 */
class OptionBarLine(context: Context) : LinearLayout(context) {

    init {
        orientation = HORIZONTAL
        setBackgroundColor(Color.WHITE)
        elevation = dp2pxf(4f)
    }

    companion object {
        val HEIGHT = dp2px(48)
    }

    fun addButton(button: OptionBarButton) {
        button.layoutParams = LayoutParams(0, MATCH_PARENT, 1f)
        addView(button)
    }

    fun getButtonAtIndex(index: Int): OptionBarButton? {
        return (getChildAt(index) as? OptionBarButton)
    }
}
