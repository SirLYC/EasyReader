package com.lyc.easyreader.bookshelf.reader

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import com.lyc.easyreader.base.ui.getDrawableAttrRes
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.utils.*
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettings

/**
 * Created by Liu Yuchuan on 2020/2/9.
 */
class ChapterDialogTopBar(context: Context) : BaseToolBar(context) {
    val rightButton = TextView(context)

    companion object {
        val VIEW_ID_CHAPTER_REVERSE = generateNewViewId()
    }

    init {
        paddingStatusBar = false
        val pageStyle = ReaderSettings.instance.pageStyle.value
        val fontColor = pageStyle.fontColor
        leftButton?.setImageDrawable(getDrawableRes(com.lyc.easyreader.api.R.drawable.ic_close_24dp)?.apply {
            changeToColor(fontColor)
        })
        titleTv.setTextColor(fontColor)
        rightButton.apply {
            id = VIEW_ID_CHAPTER_REVERSE
            background = getDrawableAttrRes(android.R.attr.selectableItemBackground)
            setTextColor(fontColor)
            textSizeInPx = dp2pxf(14f)
            setPadding(dp2px(16), dp2px(12), dp2px(16), dp2px(12))
            paint.isFakeBoldText = true
            gravity = Gravity.CENTER
            addView(rightButton, LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply {
                gravity = Gravity.RIGHT
            })
        }
        background = null
        elevation = 0f
    }

    override fun setBarClickListener(clickListener: OnClickListener) {
        super.setBarClickListener(clickListener)
        rightButton.setOnClickListener(clickListener)
    }
}
