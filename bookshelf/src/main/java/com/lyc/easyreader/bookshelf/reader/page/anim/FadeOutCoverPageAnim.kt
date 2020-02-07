package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.graphics.withSave
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/2/7.
 */
class FadeOutCoverPageAnim(
    screenWidth: Int,
    screenHeight: Int,
    view: View?,
    listener: OnPageChangeListener
) : PageAnimation(screenWidth, screenHeight, view, listener) {

    companion object {
        private const val MIN_SCALE = 0.7f
    }

    private val nextSrcRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val nextDestRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val alphaPaint = Paint()
    private val matrix = Matrix()

    override fun drawMove(canvas: Canvas) {
        val dis =
            (if (direction == Direction.NEXT) startX - touchX else (viewWidth - (touchX - startX))).coerceIn(
                0f,
                viewWidth.toFloat()
            )

        val cur = if (direction == Direction.NEXT) curBitmap else nextBitmap
        val next = if (direction == Direction.NEXT) nextBitmap else curBitmap
        nextSrcRect.right = dis.roundToInt()
        nextDestRect.left = (screenWidth - dis).roundToInt()

        alphaPaint.alpha = (0xff * (1 - dis / viewWidth)).toInt()
        val scale = 1 - (dis / viewWidth) * (1 - MIN_SCALE)
        matrix.reset()
        matrix.setScale(scale, scale, viewWidth * 0.5f, viewHeight * 0.5f)
        canvas.withSave {
            concat(this@FadeOutCoverPageAnim.matrix)
            drawBitmap(cur, 0f, 0f, alphaPaint)
        }
        canvas.drawBitmap(next, nextSrcRect, nextDestRect, null)
    }
}
