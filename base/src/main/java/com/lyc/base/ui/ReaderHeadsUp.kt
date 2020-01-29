package com.lyc.base.ui

import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import com.lyc.base.ReaderApplication
import com.lyc.base.ui.theme.color_primary_light
import com.lyc.base.utils.dp2px
import com.lyc.base.utils.dp2pxf

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
object ReaderHeadsUp : Handler.Callback {
    private var currentHeadsUp: Toast? = null
    private val handler = Handler(Looper.getMainLooper(), this)

    private const val MSG_CANCEL_TOAST = 1

    fun showHeadsUp(view: View, duration: Int = Toast.LENGTH_SHORT) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            showHeadsUpInternal(
                null,
                view,
                duration,
                0,
                0
            )
        } else {
            handler.post {
                showHeadsUpInternal(
                    null,
                    view,
                    duration,
                    0,
                    0
                )
            }
        }
    }

    fun showHeadsUp(
        @StringRes textResId: Int, duration: Int = Toast.LENGTH_SHORT,
        bgColor: Int = color_primary_light,
        textColor: Int = Color.WHITE
    ) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            showHeadsUpInternal(
                getStringRes(textResId),
                null,
                duration,
                bgColor,
                textColor
            )
        } else {
            handler.post {
                showHeadsUpInternal(
                    getStringRes(textResId),
                    null,
                    duration,
                    bgColor,
                    textColor
                )
            }
        }
    }

    fun showHeadsUp(
        text: String,
        duration: Int = Toast.LENGTH_SHORT,
        bgColor: Int = color_primary_light,
        textColor: Int = Color.WHITE
    ) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            showHeadsUpInternal(
                text,
                null,
                duration,
                bgColor,
                textColor
            )
        } else {
            handler.post {
                showHeadsUpInternal(
                    text,
                    null,
                    duration,
                    bgColor,
                    textColor
                )
            }
        }
    }

    @MainThread
    private fun showHeadsUpInternal(
        text: String?,
        view: View?,
        duration: Int,
        bgColor: Int,
        textColor: Int
    ) {
        if (text == null && view == null) {
            return
        }
        cancelToast()
        val context = ReaderApplication.appContext()
        val toast = Toast(context)
        val contentView: View = if (text != null) {
            TextView(context).apply {
                val bg = PaintDrawable(bgColor)
                bg.setCornerRadius(dp2pxf(8f))
                background = bg
                this.text = text
                setPadding(dp2px(12), dp2px(16), dp2px(12), dp2px(16))
                setTextColor(textColor)
                paint.isFakeBoldText = true
                setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(14f))
                this.gravity = Gravity.CENTER or Gravity.LEFT
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    leftMargin = dp2px(16)
                    rightMargin = dp2px(16)
                }
            }
        } else {
            view!!
        }

        val params = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            leftMargin = dp2px(16)
            rightMargin = dp2px(16)
        }
        toast.setGravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, 0)
        toast.view = FrameLayout(context).apply {
            addView(contentView, params)
        }
        toast.duration = Toast.LENGTH_LONG
        handler.removeMessages(MSG_CANCEL_TOAST)
        toast.show()
        currentHeadsUp = toast

        if (duration == Toast.LENGTH_LONG) {
            return
        }

        val delay = if (duration == Toast.LENGTH_SHORT) {
            1500L
        } else {
            duration.toLong()
        }

        handler.sendEmptyMessageDelayed(
            MSG_CANCEL_TOAST, delay
        )
    }

    @MainThread
    private fun cancelToast() {
        currentHeadsUp?.let {
            it.cancel()
            currentHeadsUp = null
        }

    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_CANCEL_TOAST -> {
                cancelToast()
            }
        }

        return true
    }
}
