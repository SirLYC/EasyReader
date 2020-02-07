package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.*
import android.view.View
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.roundToInt
import kotlin.math.sqrt

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

    private val camera = Camera()
    private val matrix = Matrix()
    private val srcRect = Rect(0, 0, viewWidth, viewHeight)
    private val destRect = Rect(0, 0, viewWidth, viewHeight)

    override fun drawMove(canvas: Canvas) {
        val dis =
            (if (direction == Direction.NEXT) startX - touchX else (viewWidth - (touchX - startX))).coerceIn(
                0f,
                viewWidth.toFloat()
            )

        val cur = if (direction == Direction.NEXT) curBitmap else nextBitmap
        val next = if (direction == Direction.NEXT) nextBitmap else curBitmap

        // 这两种情况会导致使用射影定理除数为0
        if (dis == 0f) {
            canvas.drawBitmap(cur, 0f, 0f, null)
            return
        }

        if (dis.toInt() == viewWidth) {
            canvas.drawBitmap(next, 0f, 0f, null)
            return
        }

        // 射影定理
        val visibleNextWidth = sqrt(dis * viewWidth)
        val visibleCurWidth = sqrt((viewWidth - dis) * viewWidth)
        val curAngle = asin(dis / visibleNextWidth)
        camera.save()
        camera.translate(viewWidth - dis, 0f, 0f)
        camera.rotateY(-curAngle)
        camera.translate(dis - viewWidth, 0f, 0f)
        camera.getMatrix(matrix)
        camera.restore()
        canvas.save()
        canvas.concat(matrix)
        srcRect.left = (viewWidth - visibleCurWidth).roundToInt()
        srcRect.right = viewWidth
        destRect.left = 0
        destRect.right = (viewWidth - dis).roundToInt()
        canvas.drawBitmap(
            cur,
            srcRect,
            destRect,
            null
        )
        canvas.restore()

        val nextAngle = (PI * 0.5f - curAngle).toFloat()
        camera.save()
        camera.translate(viewWidth - dis, 0f, 0f)
        camera.rotateY(nextAngle)
        camera.translate(dis - viewWidth, 0f, 0f)
        camera.getMatrix(matrix)
        camera.restore()
        canvas.save()
        canvas.concat(matrix)
        srcRect.left = 0
        srcRect.right = visibleNextWidth.roundToInt()
        destRect.left = (viewWidth - dis).roundToInt()
        destRect.right = viewWidth
        canvas.drawRect(destRect, Paint().apply {
            color = Color.WHITE
        })
        canvas.drawBitmap(
            next,
            srcRect,
            destRect,
            null
        )
        canvas.restore()
    }
}
