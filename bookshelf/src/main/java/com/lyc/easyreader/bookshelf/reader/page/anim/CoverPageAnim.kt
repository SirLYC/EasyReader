package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.View
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/2/5.
 */
class CoverPageAnim(
    w: Int,
    h: Int,
    view: View?,
    listener: OnPageChangeListener
) : PageAnimation(w, h, view, listener) {
    private val curSrcRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val curDestRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val nextSrcRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val nextDestRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val backShadowDrawableRL by lazy {
        val colors = intArrayOf(0x00000000, 0x66000000)
        GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, colors
        ).apply {
            gradientType = GradientDrawable.LINEAR_GRADIENT
        }
    }

    override fun drawMove(canvas: Canvas) {
        val dis =
            (if (direction == Direction.NEXT) startX - touchX else (viewWidth - (touchX - startX))).coerceIn(
                0f,
                viewWidth.toFloat()
            )

        val cur = if (direction == Direction.NEXT) curBitmap else nextBitmap
        val next = if (direction == Direction.NEXT) nextBitmap else curBitmap
        curSrcRect.right = (screenWidth - dis).roundToInt()
        curDestRect.right = curSrcRect.right
        nextSrcRect.right = dis.roundToInt()
        nextDestRect.left = curSrcRect.right
        canvas.drawBitmap(cur, curSrcRect, curDestRect, null)
        canvas.drawBitmap(next, nextSrcRect, nextDestRect, null)
        // 阴影
        backShadowDrawableRL.setBounds(nextDestRect.left - 30, 0, nextDestRect.left, screenHeight)
        backShadowDrawableRL.draw(canvas)
    }
}
