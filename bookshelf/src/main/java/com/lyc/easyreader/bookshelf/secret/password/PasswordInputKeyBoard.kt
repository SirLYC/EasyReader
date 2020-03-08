package com.lyc.easyreader.bookshelf.secret.password

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.lyc.easyreader.base.utils.dp2px

/**
 * Created by Liu Yuchuan on 2020/3/8.
 */
class PasswordInputKeyBoard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var buttonHeight = 0
    var buttonWidth = 0
    var buttonPadding = dp2px(16)
    // location of button's left-top vertex
    private val buttonLocationX = IntArray(10)
    private val buttonLocationY = IntArray(10)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buttonWidth = w / 4 - 2 * buttonPadding
        buttonHeight = h / 3 - 2 * buttonPadding
        var x = 0
        var y = 0
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                val index = i * 3 + j
                buttonLocationX[index] = x + buttonPadding
                buttonLocationX[index] = y + buttonPadding
                x += buttonWidth
            }
            y += buttonWidth
            x = 0
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (buttonWidth <= 0 || buttonHeight <= 0) {
            return
        }

        for (i in buttonLocationX.indices) {
            val number = (i + 1) % 10
        }
    }
}
