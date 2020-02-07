package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.Canvas
import android.graphics.Matrix
import android.view.View
import androidx.core.graphics.withSave
import com.lyc.easyreader.base.utils.LogUtils
import java.lang.Math.PI
import kotlin.math.tan

/**
 * Created by Liu Yuchuan on 2020/2/7.
 */
class TurntableCoverPageAnim(
    screenWidth: Int,
    screenHeight: Int,
    view: View?,
    listener: OnPageChangeListener
) : PageAnimation(screenWidth, screenHeight, view, listener) {
    companion object {
        // 旋转角
        // 旋转中心是由两屏底部向外作垂线所得
        const val INIT_ROTATE = PI / 4
        val INIT_ROTATE_DEGREE = Math.toDegrees(INIT_ROTATE)
    }

    // 三角形全等
    private val rotateX = screenWidth / 2f
    // 直角三角形，x/2对角是INIT_ROTATE / 2
    private val rotateY = (screenHeight + rotateX / tan(INIT_ROTATE / 2)).toFloat()
    private val rotateMatrix = Matrix()

    override
    fun drawMove(canvas: Canvas) {
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
        val curAngle = -(INIT_ROTATE_DEGREE * interpolator).toFloat()
        val nextAngle = (curAngle + INIT_ROTATE_DEGREE).toFloat()

        LogUtils.d("AAA", "Cur=$curAngle next=$nextAngle")

        // next
        canvas.withSave {
            rotateMatrix.reset()
            rotateMatrix.setRotate(nextAngle, rotateX, rotateY)
            concat(rotateMatrix)
            drawBitmap(next, 0f, 0f, null)
        }

        // current
        canvas.withSave {
            rotateMatrix.reset()
            rotateMatrix.setRotate(curAngle, rotateX, rotateY)
            concat(rotateMatrix)
            drawBitmap(cur, 0f, 0f, null)
        }
    }
}
