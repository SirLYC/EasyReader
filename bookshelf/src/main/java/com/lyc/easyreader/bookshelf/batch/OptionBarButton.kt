package com.lyc.easyreader.bookshelf.batch

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.setPadding
import com.lyc.easyreader.base.ui.getDrawableAttrRes
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.theme.color_primary_text
import com.lyc.easyreader.base.utils.addColorAlpha
import com.lyc.easyreader.base.utils.changeToColor
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.textSizeInDp

/**
 * Created by Liu Yuchuan on 2020/3/4.
 */
@SuppressLint("ViewConstructor")
class OptionBarButton(context: Context, @DrawableRes iconRes: Int, text: String) :
    FrameLayout(context) {
    val tv = TextView(context).apply { setText(text) }
    val iconIv = ImageView(context)
    private val iconDrawable: Drawable =
        getDrawableRes(iconRes)!!.apply { changeToColor(color_primary_text) }
    private val disableColor = color_primary_text.addColorAlpha((0xff * 0.5f).toInt())

    init {
        setPadding(dp2px(4))
        background = getDrawableAttrRes(android.R.attr.selectableItemBackground)
        initView()
        applyEnable(true)
    }

    override fun setEnabled(enabled: Boolean) {
        val change = enabled != this.isEnabled
        if (!change) {
            return
        }
        super.setEnabled(enabled)
        applyEnable(enabled)
    }

    private fun applyEnable(enabled: Boolean) {
        if (!enabled) {
            tv.setTextColor(disableColor)
            iconIv.alpha = 0.5f
        } else {
            tv.setTextColor(color_primary_text)
            iconIv.alpha = 1f
        }
    }

    private fun initView() {
        tv.textSizeInDp = 12f
        tv.maxLines = 1
        tv.setSingleLine()
        tv.gravity = Gravity.CENTER or Gravity.BOTTOM
        val textHeight = tv.paint.let {
            val fontMetrics = it.fontMetrics
            return@let fontMetrics.descent - fontMetrics.ascent
        }.toInt()
        addView(tv, LayoutParams(LayoutParams.MATCH_PARENT, textHeight).apply {
            gravity = Gravity.BOTTOM
        })
        iconIv.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iconIv.setImageDrawable(iconDrawable)
        addView(
            iconIv,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                bottomMargin = textHeight
            })
    }
}
