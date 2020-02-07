package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/2/5.
 */
class SlidePageAnim(
    w: Int,
    h: Int,
    view: View?,
    listener: OnPageChangeListener?
) : PageAnimation(w, h, view, listener!!) {
    private val curSrcRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val curDestRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val nextSrcRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val nextDestRect: Rect = Rect(0, 0, viewWidth, viewHeight)

    override fun drawMove(canvas: Canvas) {
        val dis =
            (if (direction == Direction.NEXT) startX - touchX else (viewWidth - (touchX - startX))).roundToInt()
                .coerceIn(
                    0,
                    viewWidth
                )

        val cur = if (direction == Direction.NEXT) curBitmap else nextBitmap
        val next = if (direction == Direction.NEXT) nextBitmap else curBitmap
        curSrcRect.left = dis
        curDestRect.right = viewWidth - dis
        nextSrcRect.right = dis
        nextDestRect.left = curDestRect.right
        canvas.drawBitmap(cur, curSrcRect, curDestRect, null)
        canvas.drawBitmap(next, nextSrcRect, nextDestRect, null)
    }
}
