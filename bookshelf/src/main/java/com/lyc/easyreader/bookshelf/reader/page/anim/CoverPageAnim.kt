package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.View
import kotlin.math.abs

/**
 * Created by Liu Yuchuan on 2020/2/5.
 */
class CoverPageAnim(
    w: Int,
    h: Int,
    view: View?,
    listener: OnPageChangeListener
) : PageAnimation(w, h, view, listener) {
    private val mSrcRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val mDestRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val mBackShadowDrawableLR: GradientDrawable
    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            nextBitmap = curBitmap.copy(Bitmap.Config.RGB_565, true)
            canvas.drawBitmap(curBitmap, 0f, 0f, null)
        } else {
            canvas.drawBitmap(nextBitmap, 0f, 0f, null)
        }
    }

    override fun drawMove(canvas: Canvas) {
        if (direction === Direction.NEXT) {
            var dis = (viewWidth - startX + touchX).toInt()
            if (dis > viewWidth) {
                dis = viewWidth
            }
            //计算bitmap截取的区域
            mSrcRect.left = viewWidth - dis
            //计算bitmap在canvas显示的区域
            mDestRect.right = dis
            canvas.drawBitmap(bgBitmap, 0f, 0f, null)
            canvas.drawBitmap(curBitmap, mSrcRect, mDestRect, null)
            addShadow(dis, canvas)
        } else {
            mSrcRect.left = (viewWidth - touchX).toInt()
            mDestRect.right = touchX.toInt()
            canvas.drawBitmap(curBitmap, 0f, 0f, null)
            canvas.drawBitmap(bgBitmap, mSrcRect, mDestRect, null)
            addShadow(touchX.toInt(), canvas)
        }
    }

    //添加阴影
    private fun addShadow(left: Int, canvas: Canvas) {
        mBackShadowDrawableLR.setBounds(left, 0, left + 30, screenHeight)
        mBackShadowDrawableLR.draw(canvas)
    }

    override fun startAnim() {
        super.startAnim()
        val dx: Int
        if (direction === Direction.NEXT) {
            if (isCancel) {
                var dis = (viewWidth - startX + touchX).toInt()
                if (dis > viewWidth) {
                    dis = viewWidth
                }
                dx = viewWidth - dis
            } else {
                dx = (-(touchX + (viewWidth - startX))).toInt()
            }
        } else {
            dx = if (isCancel) {
                (-touchX).toInt()
            } else {
                (viewWidth - touchX).toInt()
            }
        }
        //滑动速度保持一致
        val duration = 400 * abs(dx) / viewWidth
        scroller.startScroll(touchX.toInt(), 0, dx, 0, duration)
    }

    init {
        val mBackShadowColors = intArrayOf(0x66000000, 0x00000000)
        mBackShadowDrawableLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors
        )
        mBackShadowDrawableLR.gradientType = GradientDrawable.LINEAR_GRADIENT
    }
}
