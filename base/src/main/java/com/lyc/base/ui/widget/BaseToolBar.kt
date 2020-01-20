package com.lyc.base.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import com.lyc.base.R
import com.lyc.base.ui.getDrawableAttrRes
import com.lyc.base.ui.getDrawableRes
import com.lyc.base.ui.theme.color_primary_text
import com.lyc.base.utils.*

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
open class BaseToolBar(context: Context, private val paddingStatusBar: Boolean = true) :
    FrameLayout(context) {
    protected val titleTv = TextView(context)
    protected var leftButton: ImageView? = null
    var drawDivideLine = true
        set(value) {
            if (value != field) {
                field = value
                invalidate()
            }
        }

    companion object {
        private val BAR_HEIGHT = dp2px(48)

        val VIEW_ID_BAR = generateNewViewId()
        val VIEW_ID_LEFT_BUTTON = generateNewViewId()
        val VIEW_ID_TITLE = generateNewViewId()
    }

    init {
        id = VIEW_ID_BAR
        initView()
    }

    private fun initView() {
        setBackgroundColor(Color.WHITE)
        if (paddingStatusBar) {
            setPadding(0, statusBarHeight(), 0, 0)
        }
        titleTv.setTextColor(color_primary_text)
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(16f))
        titleTv.gravity = Gravity.CENTER
        titleTv.id = VIEW_ID_TITLE
        titleTv.ellipsize = TextUtils.TruncateAt.END
        titleTv.paint.isFakeBoldText = true
        titleTv.maxLines = 1
        titleTv.setSingleLine()
        addView(titleTv,
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, BAR_HEIGHT).apply {
                val dp56 = dp2px(56)
                leftMargin = dp56
                rightMargin = dp56
            }
        )
        initLeftButton()
        initRightButton()
    }

    fun setTitle(text: String?) {
        titleTv.text = text
    }

    fun setTitle(@StringRes strResId: Int) {
        titleTv.setText(strResId)
    }

    @CallSuper
    open fun setBarClickListener(clickListener: OnClickListener) {
        titleTv.setOnClickListener(clickListener)
        leftButton?.setOnClickListener(clickListener)
    }

    protected open fun initLeftButton() {
        leftButton = ImageView(context).apply {
            setPadding(dp2px(16), dp2px(12), dp2px(16), dp2px(12))
            scaleType = ImageView.ScaleType.CENTER
            id = VIEW_ID_LEFT_BUTTON
            addView(this, LayoutParams(BAR_HEIGHT, BAR_HEIGHT).apply {
                gravity = Gravity.LEFT
            })
            getDrawableRes(R.drawable.ic_arrow_back_24dp)?.let {
                val newDrawable = it.mutate()
                newDrawable.colorFilter =
                    PorterDuffColorFilter(color_primary_text, PorterDuff.Mode.SRC_ATOP)
                setImageDrawable(newDrawable)
            }
            getDrawableAttrRes(android.R.attr.selectableItemBackground)?.let {
                background = it
            }
        }
    }


    protected open fun initRightButton() {

    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (drawDivideLine) {
            canvas?.drawBottomDivideLine(width.toFloat(), height.toFloat())
        }
    }

    fun getViewHeight() = BAR_HEIGHT + paddingTop
}
