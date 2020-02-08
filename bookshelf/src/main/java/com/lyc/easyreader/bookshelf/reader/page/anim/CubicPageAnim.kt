package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Matrix
import android.view.View

/**
 * Created by Liu Yuchuan on 2020/2/7.
 * 3D立方翻页动画
 */
class CubicPageAnim(
    screenWidth: Int,
    screenHeight: Int,
    view: View?,
    listener: OnPageChangeListener
) : PageAnimation(screenWidth, screenHeight, view, listener) {

    override val needDrawBgColorWhenRunning = true
    private val camera = Camera()
    private val matrix = Matrix()

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

        val vwf = viewWidth.toFloat()

        val curAngle = dis / vwf * 90f
        camera.save()
        camera.setLocation(0f, 0f, -64f)
        camera.rotateY(-curAngle)
        camera.getMatrix(matrix)
        camera.restore()
        matrix.preTranslate(-vwf, 0f)
        matrix.postTranslate(viewWidth - dis, 0f)
        canvas.save()
        canvas.concat(matrix)
        canvas.drawBitmap(cur, 0f, 0f, null)
        canvas.restore()

        val nextAngle = 90f - curAngle
        camera.save()
        camera.setLocation(0f, 0f, -64f)
        camera.rotateY(nextAngle)
        camera.getMatrix(matrix)
        camera.restore()
        matrix.postTranslate(viewWidth - dis, 0f)
        canvas.save()
        canvas.concat(matrix)
        canvas.drawBitmap(next, 0f, 0f, null)
        canvas.restore()
    }
}
