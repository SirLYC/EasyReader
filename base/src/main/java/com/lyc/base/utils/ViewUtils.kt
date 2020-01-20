package com.lyc.base.utils

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.MainThread
import com.lyc.base.ui.theme.color_divider

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

private val divideLinePaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = color_divider
    }
}

fun Canvas.drawBottomDivideLine(width: Float, height: Float) {
    drawRect(0f, height - 1f, width, height, divideLinePaint)
}

fun Canvas.drawTopDivideLine(width: Float) {
    drawRect(0f, 0f, width, 1f, divideLinePaint)
}
