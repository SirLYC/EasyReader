package com.lyc.easyreader.bookshelf.reader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.Gravity
import android.widget.TextView
import androidx.core.view.setPadding
import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf
import com.lyc.easyreader.base.utils.textSizeInPx
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettings

/**
 * Created by Liu Yuchuan on 2020/2/9.
 */
class ChapterItemView(context: Context) : TextView(context) {
    private var bookChapter: BookChapter? = null
    private var currentChapter = -1
    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ReaderSettings.currentPageStyle.fontColor and 0x33FFFFFF
        }
    }

    init {
        setTextColor(ReaderSettings.currentPageStyle.fontColor)
        setPadding(dp2px(16))
        gravity = Gravity.LEFT or Gravity.CENTER
        textSizeInPx = dp2pxf(16f)
    }

    fun bindData(bookChapter: BookChapter?, currentChapterIndex: Int) {
        if (currentChapterIndex != this.currentChapter) {
            this.currentChapter = currentChapterIndex
            invalidate()
        }

        if (bookChapter == this.bookChapter) {
            return
        }

        if (bookChapter == null) {
            this.bookChapter = null
            return
        }

        this.bookChapter = bookChapter
        text = bookChapter.title
    }

    override fun onDraw(canvas: Canvas?) {
        val chapter = bookChapter
        if (chapter != null && chapter.order == currentChapter) {
            canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
        super.onDraw(canvas)
    }
}
