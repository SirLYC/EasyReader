package com.lyc.easyreader.base.ui

import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.core.view.setPadding
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.ui.theme.NightModeManager
import com.lyc.easyreader.base.utils.blendColor
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
object ReaderToast : Handler.Callback {
    private var currentToast: Toast? = null
    private val handler = Handler(Looper.getMainLooper(), this)

    private const val MSG_CANCEL_TOAST = 1

    fun showToast(
        @StringRes textResId: Int, duration: Int = Toast.LENGTH_SHORT,
        center: Boolean = false
    ) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            showToastInternal(
                getStringRes(textResId),
                duration,
                center
            )
        } else {
            handler.post {
                showToastInternal(
                    getStringRes(textResId),
                    duration,
                    center
                )
            }
        }
    }

    fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT, center: Boolean = false) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            showToastInternal(text, duration, center)
        } else {
            handler.post {
                showToastInternal(
                    text,
                    duration,
                    center
                )
            }
        }
    }

    @MainThread
    private fun showToastInternal(text: String, duration: Int, center: Boolean) {
        cancelToast()
        val context = ReaderApplication.appContext()
        val toast = Toast(context)
        val textView = TextView(context).apply {
            val bg = PaintDrawable(
                Color.argb(0xcc, 0, 0, 0)
            )
            bg.setCornerRadius(dp2pxf(4f))
            background = bg
            this.text = text
            setPadding(dp2px(8))
            if (NightModeManager.nightModeEnable) {
                setTextColor(blendColor(Color.WHITE, NightModeManager.NIGHT_MODE_MASK_COLOR))
            } else {
                setTextColor(Color.WHITE)
            }
            setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(14f))
            this.gravity = Gravity.CENTER
        }
        if (center) {
            toast.setGravity(Gravity.CENTER, 0, 0)
        } else {
            toast.setGravity(Gravity.BOTTOM, 0, dp2px(48))
        }
        toast.view = textView
        toast.duration = Toast.LENGTH_LONG
        handler.removeMessages(MSG_CANCEL_TOAST)
        toast.show()
        currentToast = toast

        if (duration == Toast.LENGTH_LONG) {
            return
        }

        val delay = if (duration == Toast.LENGTH_SHORT) {
            2000L
        } else {
            duration.toLong()
        }

        handler.sendEmptyMessageDelayed(
            MSG_CANCEL_TOAST, delay
        )
    }

    @MainThread
    private fun cancelToast() {
        currentToast?.let {
            it.cancel()
            currentToast = null
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
