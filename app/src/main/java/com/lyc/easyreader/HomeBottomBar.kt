package com.lyc.easyreader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.lyc.api.main.IMainTabDelegate
import com.lyc.base.getAppExtensions
import com.lyc.base.ui.getDrawableAttrRes
import com.lyc.base.ui.getDrawableRes
import com.lyc.base.ui.theme.color_accent
import com.lyc.base.ui.theme.color_primary
import com.lyc.base.utils.dp2px
import com.lyc.base.utils.dp2pxf
import com.lyc.base.utils.drawTopDivideLine
import java.util.*

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
class HomeBottomBar(context: Context) : LinearLayout(context), View.OnClickListener {

    companion object {

        val CURRENT_FILTER by lazy {
            PorterDuffColorFilter(
                color_primary,
                PorterDuff.Mode.SRC_ATOP
            )
        }
        val NORMAL_FILTER by lazy { PorterDuffColorFilter(color_accent, PorterDuff.Mode.SRC_ATOP) }
    }

    var currentId = -1
        private set
    private var viewMap = hashMapOf<Int, BottomBarButton>()

    init {
        setBackgroundColor(Color.WHITE)
        orientation = HORIZONTAL
        val tabSet = TreeSet<IMainTabDelegate>(getAppExtensions<IMainTabDelegate>())
        tabSet.forEach {
            val button = BottomBarButton(
                context,
                it.getIconDrawableResId(),
                it.getName()
            )
            button.id = it.getId()
            button.setOnClickListener(this)
            viewMap[button.id] = button
            addView(button, LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        canvas?.drawTopDivideLine(width.toFloat())
    }

    override fun onClick(v: View?) {
        v?.id?.let { id ->
            if (MainActivityDelegate.instance.isMainTabId(id)) {
                if (!changeTab(id)) {
                    MainActivityDelegate.instance.notifyTabClick(id)
                }
            }
        }
    }

    fun changeTab(newTabId: Int): Boolean {
        if (!(MainActivityDelegate.instance.isMainTabId(newTabId))) {
            return false
        }
        val oldId = currentId
        if (oldId != newTabId) {
            currentId = newTabId
            viewMap[oldId]?.current = false
            viewMap[newTabId]?.current = true
            MainActivityDelegate.instance.notifyTabChange(newTabId)
            return true
        }

        return false
    }

    private class BottomBarButton(
        context: Context,
        iconRes: Int,
        text: String
    ) : FrameLayout(context) {
        private val tv = TextView(context)
        private val iconIv = ImageView(context)
        private val iconDrawable: Drawable = getDrawableRes(iconRes)!!
        var current = false
            set(value) {
                if (value != field) {
                    field = value
                    applyCurrent()
                }
            }

        init {
            setPadding(dp2px(8))
            iconIv.setImageDrawable(iconDrawable)
            getDrawableAttrRes(android.R.attr.selectableItemBackground)?.let {
                background = it
            }
            initView(text)
        }

        private fun applyCurrent() {
            if (current) {
                iconIv.colorFilter = CURRENT_FILTER
                tv.setTextColor(color_primary)
                tv.paint.isFakeBoldText = true
            } else {
                iconIv.colorFilter = if (current) CURRENT_FILTER else NORMAL_FILTER
                tv.setTextColor(color_accent)
                tv.paint.isFakeBoldText = false
            }
        }

        private fun initView(text: String) {
            applyCurrent()
            tv.text = text
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(10f))
            tv.maxLines = 1
            tv.setSingleLine()
            tv.gravity = Gravity.CENTER or Gravity.BOTTOM
            val textHeight = tv.paint.let {
                val fontMetrics = it.fontMetrics
                return@let fontMetrics.descent - fontMetrics.ascent
            }.toInt() + dp2px(8)
            addView(tv, LayoutParams(LayoutParams.MATCH_PARENT, textHeight).apply {
                gravity = Gravity.BOTTOM
            })
            iconIv.scaleType = ImageView.ScaleType.CENTER
            addView(
                iconIv,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                    bottomMargin = textHeight
                })
        }
    }
}
