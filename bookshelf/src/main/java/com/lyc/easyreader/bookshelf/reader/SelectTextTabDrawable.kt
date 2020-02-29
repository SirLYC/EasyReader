package com.lyc.easyreader.bookshelf.reader

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.withClip
import androidx.core.graphics.withTranslation
import kotlin.math.sqrt

/**
 * Created by Liu Yuchuan on 2020/2/29.
 */
class SelectTextTabDrawable : Drawable() {

    private val sqrt2 = sqrt(2f)
    @JvmField
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val contentPath = Path()
    var upAndDown = false
        set(value) {
            if (field != value) {
                field = value
                onBoundsChange(bounds)
            }
        }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val w = bounds.width()
        val h = bounds.height()
        if (w <= 0 || h <= 0) {
            return
        }

        contentPath.reset()
        val halfW = w * 0.5f
        contentPath.moveTo(halfW, if (upAndDown) h.toFloat() else 0f)
        val r = h / (1 + sqrt2)
        if (upAndDown) {
            contentPath.arcTo(halfW - r, 0f, halfW + r, 2 * r, 135f, 270f, false)
        } else {
            contentPath.arcTo(halfW - r, h - 2 * r, halfW + r, h.toFloat(), -45f, 270f, false)
        }
        contentPath.close()
    }

    override fun draw(canvas: Canvas) {
        if (bounds.isEmpty) {
            return
        }
        canvas.withClip(bounds) {
            withTranslation(bounds.left.toFloat(), bounds.top.toFloat()) {
                canvas.drawPath(contentPath, paint)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }
}
