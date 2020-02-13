package com.lyc.easyreader.bookshelf

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.theme.color_orange
import com.lyc.easyreader.base.ui.theme.color_primary_text
import com.lyc.easyreader.base.ui.theme.color_secondary_text
import com.lyc.easyreader.base.utils.addColorAlpha
import com.lyc.easyreader.base.utils.buildCommonButtonBg
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf
import com.lyc.easyreader.bookshelf.db.BookShelfBook
import com.lyc.easyreader.bookshelf.utils.detailTimeString
import java.util.*

/**
 *
 * Created by Liu Yuchuan on 2020/1/28.
 */
class BookShelfItemView(context: Context) : FrameLayout(context) {

    private val filenameTv = TextView(context)
    private val coverImageView = ImageView(context)
    private val fileTypeTv = TextView(context)
    private val fileInfoTv = TextView(context)


    private var data: BookShelfBook? = null
    private var position = -1

    init {
        background = buildCommonButtonBg(Color.WHITE)
        setPadding(dp2px(16), dp2px(8), dp2px(16), dp2px(8))
        initView()
    }

    private fun initView() {
        val coverWidth = dp2px(51)
        addView(coverImageView, LayoutParams(coverWidth, dp2px(68)).apply {
            gravity = Gravity.LEFT or Gravity.CENTER
        })
        coverImageView.setImageDrawable(getDrawableRes(com.lyc.easyreader.api.R.drawable.ic_book_cover))

        fileTypeTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(10f))
        fileTypeTv.gravity = Gravity.CENTER
        fileTypeTv.paint.isFakeBoldText = true
        fileTypeTv.setTextColor(Color.WHITE)
        fileTypeTv.setPadding(dp2px(4), 0, dp2px(4), 0)
        fileTypeTv.background =
            PaintDrawable(color_orange.addColorAlpha((0xff * 0.4f).toInt())).apply {
                setCornerRadii(
                    floatArrayOf(
                        0f,
                        0f,
                        dp2pxf(4f),
                        dp2pxf(4f),
                        dp2pxf(4f),
                        dp2pxf(4f),
                        0f,
                        0f
                    )
                )
            }
        addView(fileTypeTv, LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.LEFT
            topMargin = dp2px(19)
        })

        val rightContainer = LinearLayout(context)
        rightContainer.orientation = LinearLayout.VERTICAL
        addView(rightContainer, LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            gravity = Gravity.LEFT or Gravity.CENTER
            leftMargin = coverWidth + dp2px(16)
        })

        filenameTv.setTextColor(color_primary_text)
        filenameTv.paint.isFakeBoldText = true
        filenameTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(16f))
        rightContainer.addView(filenameTv, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        val infoContainer = LinearLayout(context)
        infoContainer.orientation = LinearLayout.HORIZONTAL
        infoContainer.gravity = Gravity.LEFT or Gravity.CENTER
        rightContainer.addView(
            infoContainer,
            LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                topMargin = dp2px(8)
            })

        fileInfoTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(12f))
        fileInfoTv.gravity = Gravity.CENTER or Gravity.LEFT
        fileInfoTv.setTextColor(color_secondary_text)
        infoContainer.addView(
            fileInfoTv,
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        )
    }

    fun bindData(data: BookShelfBook?, position: Int) {
        this.data = data
        this.position = position
        if (data != null) {
            filenameTv.text = data.filename
            if (data.recordDesc != null) {
                fileInfoTv.text =
                    ("${data.lastAccessTime.detailTimeString()} | 上次读到：${data.recordDesc}")
            } else {
                fileInfoTv.text = ("${data.importTime.detailTimeString()} | 未读")
            }
            fileTypeTv.text = data.fileExt.toUpperCase(Locale.ENGLISH)
        }
    }
}
