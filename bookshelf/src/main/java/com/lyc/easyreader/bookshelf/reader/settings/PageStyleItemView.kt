package com.lyc.easyreader.bookshelf.reader.settings

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.view.setPadding
import com.lyc.easyreader.base.ui.theme.color_yellow
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf
import com.lyc.easyreader.base.utils.textSizeInPx
import com.lyc.easyreader.bookshelf.reader.page.PageStyle
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
class PageStyleItemView(
    context: Context
) : TextView(context), View.OnClickListener {

    private var data: PageStyle? = null
    private val backgroundDrawable = GradientDrawable()
    private val radius: Float = dp2pxf(4f)
    private var select = false

    init {
        textSizeInPx = dp2pxf(14f)
        backgroundDrawable.cornerRadius = radius
        setPadding(dp2px(8))
        setOnClickListener(this)
        text = "字体"
        minWidth = dp2px(56)
        background = backgroundDrawable
        gravity = Gravity.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        backgroundDrawable.setBounds(0, 0, w, h)
    }

    fun bindData(data: PageStyle?) {
        val select = ReaderSettings.instance.pageStyle.value == data
        if (data == this.data && select == this.select) {
            return
        }
        if (data == null) {
            return
        }
        this.data = data
        this.select = select

        if (select) {
            backgroundDrawable.setStroke(dp2pxf(1.5f).roundToInt(), color_yellow)
        } else {
            backgroundDrawable.setStroke(0, 0)
        }
        setTextColor(data.fontColor)
        backgroundDrawable.setColor(data.bgColor)
        invalidate()
    }

    override fun onClick(v: View?) {
        val data = this.data
        if (data != null) {
            val stylePrefValue = ReaderSettings.instance.pageStyle
            if (stylePrefValue.value != data) {
                stylePrefValue.value = data
            }
        }
    }
}
