package com.lyc.easyreader.base.preference.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.ui.getDrawableAttrRes
import com.lyc.easyreader.base.ui.theme.color_divider
import com.lyc.easyreader.base.ui.theme.color_primary_text
import com.lyc.easyreader.base.ui.theme.color_secondary_text
import com.lyc.easyreader.base.utils.buildCommonButtonTextColor
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.textSizeInDp

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
open class BaseSettingItemView(
    title: String = "",
    desc: String = ""
) : LinearLayout(ReaderApplication.appContext()) {
    val titleTv = TextView(context).apply {
        setTextColor(buildCommonButtonTextColor(color_primary_text))
        textSizeInDp = 16f
        text = title
    }
    val descTv = TextView(context).apply {
        setTextColor(buildCommonButtonTextColor(color_secondary_text))
        textSizeInDp = 14f
        text = desc
    }

    private val dividerMargin = dp2px(8)
    private val dividerRect = Rect()
    private val dividerPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = color_divider
        }
    }

    init {
        background = LayerDrawable(
            arrayOf(
                ColorDrawable(Color.WHITE),
                getDrawableAttrRes(android.R.attr.selectableItemBackground)
            )
        )
        isClickable = true
        gravity = Gravity.CENTER
        orientation = HORIZONTAL
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        initView()
    }

    private fun initView() {
        setPadding(dp2px(24), dp2px(16), dp2px(24), dp2px(16))
        addView(LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(0, 0, dp2px(16), 0)
            addView(titleTv, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            addView(descTv, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        }, LayoutParams(0, WRAP_CONTENT, 1f))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        dividerRect.set(dividerMargin, h - 1, w - dividerMargin, h)
    }

    var drawDivider = true
        set(value) {
            if (field != value) {
                field = value
            }
        }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (drawDivider) {
            canvas.drawRect(dividerRect, dividerPaint)
        }
    }
}
