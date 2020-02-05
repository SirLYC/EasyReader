package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View

/**
 * Created by Liu Yuchuan on 2020/2/5.
 */
class SlidePageAnim(
    w: Int,
    h: Int,
    view: View?,
    listener: OnPageChangeListener?
) : PageAnimation(w, h, view, listener!!) {
    private val srcRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val destRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val nextSrcRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    private val nextDestRect: Rect = Rect(0, 0, viewWidth, viewHeight)
    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            canvas.drawBitmap(curBitmap, 0f, 0f, null)
        } else {
            canvas.drawBitmap(nextBitmap, 0f, 0f, null)
        }
    }

    override fun drawMove(canvas: Canvas) {
        var dis: Int
        if (direction === Direction.NEXT) { //左半边的剩余区域
            dis = (screenWidth - startX + touchX).toInt()
            if (dis > screenWidth) {
                dis = screenWidth
            }
            //计算bitmap截取的区域
            srcRect.left = screenWidth - dis
            //计算bitmap在canvas显示的区域
            destRect.right = dis
            //计算下一页截取的区域
            nextSrcRect.right = screenWidth - dis
            //计算下一页在canvas显示的区域
            nextDestRect.left = dis
            canvas.drawBitmap(nextBitmap, nextSrcRect, nextDestRect, null)
            canvas.drawBitmap(curBitmap, srcRect, destRect, null)
        } else {
            dis = (touchX - startX).toInt()
            if (dis < 0) {
                dis = 0
                startX = touchX
            }
            srcRect.left = screenWidth - dis
            destRect.right = dis
            //计算下一页截取的区域
            nextSrcRect.right = screenWidth - dis
            //计算下一页在canvas显示的区域
            nextDestRect.left = dis
            canvas.drawBitmap(curBitmap, nextSrcRect, nextDestRect, null)
            canvas.drawBitmap(nextBitmap, srcRect, destRect, null)
        }
    }

    override fun startAnim() {
        super.startAnim()
        val dx: Int
        if (direction === Direction.NEXT) {
            if (isCancel) {
                var dis = (screenWidth - startX + touchX).toInt()
                if (dis > screenWidth) {
                    dis = screenWidth
                }
                dx = screenWidth - dis
            } else {
                dx = (-(touchX + (screenWidth - startX))).toInt()
            }
        } else {
            dx = if (isCancel) {
                (-Math.abs(touchX - startX)).toInt()
            } else {
                (screenWidth - (touchX - startX)).toInt()
            }
        }
        //滑动速度保持一致
        val duration = 400 * Math.abs(dx) / screenWidth
        scroller.startScroll(touchX.toInt(), 0, dx, 0, duration)
    }

}
