package com.lyc.easyreader.bookshelf.batch

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.PaintDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.ui.theme.color_orange
import com.lyc.easyreader.base.ui.theme.color_orange_deep
import com.lyc.easyreader.base.ui.theme.color_primary_text
import com.lyc.easyreader.base.ui.theme.color_secondary_text
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf
import com.lyc.easyreader.base.utils.drawBottomDivideLine
import com.lyc.easyreader.bookshelf.utils.toFileTimeString
import java.util.*
import kotlin.math.roundToInt

/**
 *
 * Created by Liu Yuchuan on 2020/1/24.
 */
class BookBatchItemView(
    context: Context
) : LinearLayout(context) {

    private val filenameTv = TextView(context)
    private val fileTypeTv = TextView(context)
    private val fileInfoTv = TextView(context)

    private var data: BookFile? = null
    private var position = -1

    init {
        orientation = VERTICAL
        setPadding(dp2px(16), dp2px(8), dp2px(16), dp2px(8))
        initView()
    }

    private fun initView() {

        filenameTv.setTextColor(color_primary_text)
        filenameTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(16f))
        addView(filenameTv, LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        val infoContainer = LinearLayout(context)
        infoContainer.orientation = HORIZONTAL
        infoContainer.gravity = Gravity.START or Gravity.CENTER
        addView(infoContainer,
            LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { topMargin = dp2px(8) })

        fileTypeTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(12f))
        fileTypeTv.gravity = Gravity.CENTER
        fileTypeTv.setTextColor(color_orange_deep)
        fileTypeTv.setPadding(dp2px(4), fileTypeTv.paddingTop, dp2px(4), fileTypeTv.paddingRight)
        fileTypeTv.background = PaintDrawable(color_orange).apply {
            alpha = ((0xFF * 0.1f).roundToInt() and 0xFF)
            setCornerRadius(dp2pxf(2f))
        }
        infoContainer.addView(
            fileTypeTv,
            LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        )

        fileInfoTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(12f))
        fileInfoTv.gravity = Gravity.CENTER
        fileInfoTv.setTextColor(color_secondary_text)
        fileInfoTv.setPadding(dp2px(4), fileInfoTv.paddingTop, dp2px(4), fileInfoTv.paddingRight)
        infoContainer.addView(
            fileInfoTv,
            LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                leftMargin = dp2px(8)
            })
    }


    fun bindData(data: BookFile?, position: Int) {
        this.data = data
        this.position = position
        if (data != null) {
            filenameTv.text = data.filename
            fileTypeTv.text = data.fileExt?.toUpperCase(Locale.getDefault())
            fileInfoTv.text = data.importTime.toFileTimeString()
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        canvas?.drawBottomDivideLine(width.toFloat(), height.toFloat())
    }
}
