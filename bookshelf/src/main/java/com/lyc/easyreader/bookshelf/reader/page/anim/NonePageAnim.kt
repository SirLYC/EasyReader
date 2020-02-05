package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.Canvas
import android.view.View

/**
 * Created by Liu Yuchuan on 2020/2/5.
 */
class NonePageAnim(
    w: Int,
    h: Int,
    view: View?,
    listener: OnPageChangeListener
) : PageAnimation(w, h, view, listener) {

    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            canvas.drawBitmap(curBitmap, 0f, 0f, null)
        } else {
            bgBitmap.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    override fun drawMove(canvas: Canvas) {
        if (isCancel) {
            canvas.drawBitmap(curBitmap, 0f, 0f, null)
        } else {
            bgBitmap.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    override fun startAnim() {}
}
