package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/2/5.
 */
class FadeInFadeOutPageAnim(
    w: Int,
    h: Int,
    view: View?,
    listener: OnPageChangeListener?
) : PageAnimation(w, h, view, listener!!) {
    override val needDrawBgColorWhenRunning = true
    private val curSrcRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val curDestRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val nextSrcRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val nextDestRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val bitmapPaint = Paint()

    override fun drawMove(canvas: Canvas) {
        val dis =
            (if (direction == Direction.NEXT) startX - touchX else (viewWidth - (touchX - startX))).roundToInt()
                .coerceIn(
                    0,
                    viewWidth
                )

        val nextAlpha = (0xff * dis.toFloat() / viewWidth).roundToInt()
        val cur = if (direction == Direction.NEXT) curBitmap else nextBitmap
        val next = if (direction == Direction.NEXT) nextBitmap else curBitmap
        curSrcRect.left = dis
        curDestRect.right = viewWidth - dis
        nextSrcRect.right = dis
        nextDestRect.left = curDestRect.right
        bitmapPaint.alpha = 0xff - nextAlpha
        canvas.drawBitmap(cur, curSrcRect, curDestRect, bitmapPaint)
        bitmapPaint.alpha = nextAlpha
        canvas.drawBitmap(next, nextSrcRect, nextDestRect, bitmapPaint)
    }
}
