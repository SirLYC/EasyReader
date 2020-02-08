package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.view.View
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/2/7.
 * 竖直中心3D旋转动画
 */
class RotatePageAnim(
    rotateCenterRation: Float,
    screenWidth: Int,
    screenHeight: Int,
    view: View?,
    listener: OnPageChangeListener
) : PageAnimation(screenWidth, screenHeight, view, listener) {

    override val needDrawBgColorWhenRunning = true
    private val camera = Camera()
    private val matrix = Matrix()
    private val bitmapPaint = Paint()
    private val rotateCenter = viewWidth * rotateCenterRation


    override fun drawMove(canvas: Canvas) {
        val dis =
            (if (direction == Direction.NEXT) startX - touchX else (viewWidth - (touchX - startX))).coerceIn(
                0f,
                viewWidth.toFloat()
            )

        val cur = if (direction == Direction.NEXT) curBitmap else nextBitmap
        val next = if (direction == Direction.NEXT) nextBitmap else curBitmap

        if (dis == 0f) {
            canvas.drawBitmap(cur, 0f, 0f, null)
            return
        }

        if (dis.toInt() == viewWidth) {
            canvas.drawBitmap(next, 0f, 0f, null)
            return
        }

        val interpolator = dis / viewWidth
        val curAngle = interpolator * 90f
        val nextAlpha = (0xff * interpolator).roundToInt()
        camera.save()
        camera.setLocation(0f, 0f, -64f)
        camera.rotateY(-curAngle)
        camera.getMatrix(matrix)
        camera.restore()
        matrix.preTranslate(-rotateCenter, 0f)
        matrix.postTranslate(rotateCenter, 0f)
        canvas.save()
        canvas.concat(matrix)
        bitmapPaint.alpha = 0xff - nextAlpha
        canvas.drawBitmap(cur, 0f, 0f, bitmapPaint)
        canvas.restore()

        val nextAngle = 90 - curAngle
        camera.save()
        camera.setLocation(0f, 0f, -64f)
        camera.rotateY(nextAngle)
        camera.getMatrix(matrix)
        camera.restore()
        matrix.preTranslate(-rotateCenter, 0f)
        matrix.postTranslate(rotateCenter, 0f)
        canvas.save()
        canvas.concat(matrix)
        bitmapPaint.alpha = nextAlpha
        canvas.drawBitmap(next, 0f, 0f, bitmapPaint)
        canvas.restore()
    }
}
