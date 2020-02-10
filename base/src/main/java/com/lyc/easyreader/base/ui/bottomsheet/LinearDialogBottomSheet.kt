package com.lyc.easyreader.base.ui.bottomsheet

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.lyc.easyreader.base.ui.getDrawableAttrRes
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.theme.color_bottom_sheet_item
import com.lyc.easyreader.base.utils.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/2/10.
 */
class LinearDialogBottomSheet(context: Context) : BaseDialogBottomSheet(context),
    View.OnClickListener {
    companion object {
        val ITEM_HEIGHT = dp2px(56)
    }

    private val rootView = LinearLayout(context)
    private var currentIndex = 0
    var itemClickListener: ((itemId: Int, view: View) -> Unit)? = null
    var bgColor
        get() = 0
        set(value) {
            rootView.setBackgroundColor(value)
        }
    var title: String? = null

    init {
        rootView.orientation = LinearLayout.VERTICAL
        rootView.setBackgroundColor(Color.WHITE)
    }


    fun addItem(
        title: String,
        iconRes: Int = 0,
        color: Int = color_bottom_sheet_item,
        changeDrawableColor: Boolean = true
    ): Int {
        return addItem(
            title,
            if (iconRes == 0) {
                null
            } else {
                getDrawableRes(iconRes)?.apply {
                    if (changeDrawableColor) {
                        changeToColor(color)
                    }
                }
            },
            color
        )
    }


    fun addItem(title: String, icon: Drawable?, color: Int = color_bottom_sheet_item): Int {
        val item = ItemView(context, title, icon, currentIndex++, color)
        item.setOnClickListener(this)
        rootView.addView(item, LinearLayout.LayoutParams(MATCH_PARENT, ITEM_HEIGHT))
        return item.itemId
    }

    private class ItemView(
        context: Context,
        title: String,
        icon: Drawable?,
        val itemId: Int,
        color: Int
    ) :
        LinearLayout(context) {
        init {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            background = getDrawableAttrRes(android.R.attr.selectableItemBackground)
            initView(title, icon, color)
        }

        private fun initView(title: String, icon: Drawable?, color: Int) {
            if (icon != null) {
                val iconView = ImageView(context)
                iconView.setImageDrawable(icon)
                iconView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                addView(iconView, LayoutParams(dp2px(56), dp2px(48)))
                iconView.setPadding(dp2px(16), dp2px(12), dp2px(16), dp2px(12))
            }

            val textView = TextView(context)
            textView.text = title
            textView.textSizeInDp = 16f
            textView.maxLines = 1
            textView.isSingleLine = true
            textView.setTextColor(color)
            addView(textView, LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1f).apply {
                leftMargin = dp2px(if (icon == null) 16 else 8)
            })
        }
    }

    override fun onClick(v: View?) {
        (v as? ItemView)?.let { view ->
            itemClickListener?.run {
                invoke(view.itemId, view)
            }
            dismiss()
        }
    }

    override fun show() {
        val contentView = NestedScrollView(context)
        contentView.addView(rootView)
        setContentView(contentView)
        val height = min(
            rootView.childCount * ITEM_HEIGHT, if (getScreenOrientation() % 180 == 0) {
                max((deviceHeight() * 0.75f).roundToInt(), dp2px(480))
            } else {
                max((deviceHeight() * 0.75f).roundToInt(), dp2px(270))
            }
        )
        window?.run {
            val lp = attributes
            attributes.height = height
            attributes = lp
        }
        super.show()
    }
}
