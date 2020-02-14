package com.lyc.easyreader.bookshelf.scan

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.PaintDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.theme.color_orange
import com.lyc.easyreader.base.ui.theme.color_orange_deep
import com.lyc.easyreader.base.ui.theme.color_primary_text
import com.lyc.easyreader.base.ui.theme.color_secondary_text
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf
import com.lyc.easyreader.bookshelf.utils.toFileSizeString
import com.lyc.easyreader.bookshelf.utils.toFileTimeString
import java.util.*
import kotlin.math.roundToInt

/**
 *
 * Created by Liu Yuchuan on 2020/1/24.
 */
class BookScanItemView(
    context: Context,
    private val positionSelectController: PositionSelectController
) : FrameLayout(context), View.OnClickListener, PositionSelectController.PositionSelectListener {

    private val filenameTv = TextView(context)
    private val checkBox = ImageView(context)
    private val fileTypeTv = TextView(context)
    private val fileInfoTv = TextView(context)

    private val checkedDrawable =
        getDrawableRes(com.lyc.easyreader.base.R.drawable.ic_check_circle_24dp)?.apply {
            colorFilter = PorterDuffColorFilter(color_orange, PorterDuff.Mode.SRC_ATOP)
        }
    private val uncheckDrawable =
        getDrawableRes(com.lyc.easyreader.base.R.drawable.ic_radio_button_unchecked_24dp)?.apply {
            colorFilter = PorterDuffColorFilter(color_orange, PorterDuff.Mode.SRC_ATOP)
        }

    private var data: BookScanItem? = null
    private var position = -1

    init {
        setOnClickListener(this)
        setPadding(dp2px(16), dp2px(8), dp2px(16), dp2px(8))
        initView()
    }

    private fun initView() {
        val dp24 = dp2px(24)
        addView(checkBox, LayoutParams(dp24, dp24).apply {
            gravity = Gravity.LEFT or Gravity.CENTER
        })

        val rightContainer = LinearLayout(context)
        rightContainer.orientation = LinearLayout.VERTICAL
        addView(rightContainer, LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            gravity = Gravity.LEFT or Gravity.CENTER
            leftMargin = dp24 + dp2px(16)
        })

        filenameTv.setTextColor(color_primary_text)
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

        fileTypeTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(12f))
        fileTypeTv.gravity = Gravity.CENTER
        fileTypeTv.setTextColor(color_orange_deep)
        fileTypeTv.setPadding(dp2px(4), fileTypeTv.paddingTop, dp2px(4), fileTypeTv.paddingRight)
        fileTypeTv.background = PaintDrawable(color_orange).apply {
            alpha = ((0xFF * 0.1f).roundToInt() and 0xFF)
            setCornerRadius(dp2pxf(2f))
        }
        infoContainer.addView(fileTypeTv, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))

        fileInfoTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(12f))
        fileInfoTv.gravity = Gravity.CENTER
        fileInfoTv.setTextColor(color_secondary_text)
        fileInfoTv.setPadding(dp2px(4), fileInfoTv.paddingTop, dp2px(4), fileInfoTv.paddingRight)
        infoContainer.addView(
            fileInfoTv,
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                leftMargin = dp2px(8)
            })
    }

    override fun onClick(v: View?) {
        if (position >= 0) {
            data?.let {
                positionSelectController.flipSelectState(position)
            }
        }
    }

    private fun applyCheckState() {
        checkBox.setImageDrawable(if (positionSelectController.selectContains(position)) checkedDrawable else uncheckDrawable)
    }

    fun bindData(data: BookScanItem?, position: Int) {
        this.data = data
        this.position = position
        if (data != null) {
            filenameTv.text = data.name
            fileTypeTv.text = data.ext?.toUpperCase(Locale.getDefault())
            fileInfoTv.text =
                ("${data.lastModified.toFileTimeString()}  ${data.length.toFileSizeString()}")
            applyCheckState()
            positionSelectController.addListener(this)
        } else {
            positionSelectController.removeListener(this)
        }
    }

    override fun onPositionSelect(position: Int, select: Boolean) {
        if (position == this.position && data != null) {
            applyCheckState()
        }
    }

    override fun onSelectAllChange(select: Boolean) {
        if (data != null) {
            applyCheckState()
        }
    }
}
