package com.lyc.easyreader.base.utils

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.annotation.MainThread
import com.lyc.easyreader.base.ui.getDrawableAttrRes
import com.lyc.easyreader.base.ui.theme.color_divider
import com.lyc.easyreader.base.ui.theme.color_orange
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
private var currentViewId = 0

@MainThread
fun generateNewViewId(): Int {
    if (currentViewId == Int.MAX_VALUE) {
        currentViewId = 1
        return 1
    }

    return ++currentViewId
}

private var currentRequestCode = 0
@MainThread
fun generateNewRequestCode(): Int {
    // Fragment和FragmentActivity支持RequestCode最大为0xffff
    if (currentRequestCode == 0xffff) {
        currentRequestCode = 1
        return 1
    }

    return ++currentRequestCode
}

fun Drawable.changeToColor(newColor: Int) {
    colorFilter = PorterDuffColorFilter(newColor, PorterDuff.Mode.SRC_ATOP)
}

private val divideLinePaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = color_divider
    }
}

fun Canvas.drawBottomDivideLine(width: Float, height: Float, lineSize: Float = 1f) {
    drawRect(0f, height - lineSize, width, height, divideLinePaint)
}

fun Canvas.drawTopDivideLine(width: Float, lineSize: Float = 1f) {
    drawRect(0f, 0f, width, lineSize, divideLinePaint)
}

fun buildCommonButtonBg(color: Int = color_orange, outline: Boolean = false): Drawable {
    val commonBg = GradientDrawable().apply {
        cornerRadius = dp2pxf(4f)
        if (outline) {
            setStroke(dp2pxf(1f).roundToInt(), color)
        } else {
            setColor(color)
        }
    }
    val commonBgNotEnabled = GradientDrawable().apply {
        cornerRadius = dp2pxf(4f)
        if (outline) {
            setStroke(dp2pxf(1f).roundToInt(), color)
        } else {
            setColor(color)
        }
        alpha = 0x7f
    }
    val result = StateListDrawable()
    result.addState(intArrayOf(-android.R.attr.state_pressed), commonBg)
    result.addState(
        intArrayOf(android.R.attr.state_pressed),
        LayerDrawable(
            arrayOf(
                commonBg,
                getDrawableAttrRes(android.R.attr.selectableItemBackground)
            )
        )
    )
    result.addState(
        intArrayOf(-android.R.attr.state_enabled),
        commonBgNotEnabled
    )
    return result
}

fun buildCommonButtonTextColor(color: Int = color_orange) = ColorStateList(
    arrayOf(
        intArrayOf(android.R.attr.state_enabled),
        intArrayOf(-android.R.attr.state_enabled)
    ),
    intArrayOf(color, color.addColorAlpha(0x7f))
)

fun Int.addColorAlpha(@IntRange(from = 0, to = 0xff) alpha: Int) =
    Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))

fun blendColor(bg: Int, fg: Int): Int {
    val scr = Color.red(fg)
    val scg = Color.green(fg)
    val scb = Color.blue(fg)
    val sa = fg ushr 24
    val dcr = Color.red(bg)
    val dcg = Color.green(bg)
    val dcb = Color.blue(bg)
    val colorR = dcr * (0xff - sa) / 0xff + scr * sa / 0xff
    val colorG = dcg * (0xff - sa) / 0xff + scg * sa / 0xff
    val colorB = dcb * (0xff - sa) / 0xff + scb * sa / 0xff
    return ((colorR shl 16) + (colorG shl 8) + colorB) or (0xff000000.toInt())
}

fun isLightColor(color: Int): Boolean {
    // RGB 转 YUV
    val darkness: Double =
        1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(
            color
        )) / 255
    return darkness < 0.5
}

private val paint by lazy { Paint() }
fun measureSingleLineTextHeight(text: String, size: Float): Float {
    paint.textSize = size
    return paint.fontMetrics.run { descent - ascent }
}

var TextView.textSizeInPx
    get() = textSize
    set(value) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
    }
