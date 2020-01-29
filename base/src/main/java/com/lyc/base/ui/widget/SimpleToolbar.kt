package com.lyc.base.ui.widget

import android.content.Context
import android.view.Gravity
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.lyc.base.ui.getDrawableAttrRes
import com.lyc.base.ui.getDrawableRes
import com.lyc.base.ui.theme.color_primary_text
import com.lyc.base.utils.changeToColor
import com.lyc.base.utils.dp2px
import com.lyc.base.utils.generateNewViewId

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class SimpleToolbar(context: Context, @DrawableRes private val leftIconRes: Int) :
    BaseToolBar(context) {

    companion object {
        val VIEW_ID_RIGHT_BUTTON = generateNewViewId()
    }

    private val rightButton: ImageView = ImageView(context)

    init {
        rightButton.run {
            setPadding(dp2px(16), dp2px(12), dp2px(16), dp2px(12))
            scaleType = ImageView.ScaleType.CENTER
            id = VIEW_ID_RIGHT_BUTTON
            addView(this, LayoutParams(BAR_HEIGHT, BAR_HEIGHT).apply {
                gravity = Gravity.RIGHT
            })
            getDrawableRes(leftIconRes)?.let {
                it.changeToColor(color_primary_text)
                setImageDrawable(it)
            }
            getDrawableAttrRes(android.R.attr.selectableItemBackground)?.let {
                background = it
            }
        }
    }

    override fun setBarClickListener(clickListener: OnClickListener) {
        super.setBarClickListener(clickListener)
        rightButton.setOnClickListener(clickListener)
    }
}
