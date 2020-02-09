package com.lyc.easyreader.bookshelf.reader

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.view.setPadding
import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.base.ui.getDrawableAttrRes
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf
import com.lyc.easyreader.base.utils.textSizeInPx
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettings

/**
 * Created by Liu Yuchuan on 2020/2/9.
 */
class ChapterItemView(
    context: Context,
    private val onItemClick: (pos: Int, bookChapter: BookChapter, view: View) -> Unit
) : TextView(context), View.OnClickListener {
    private var bookChapter: BookChapter? = null

    init {
        setTextColor(ReaderSettings.instance.pageStyle.value.fontColor)
        background = getDrawableAttrRes(android.R.attr.selectableItemBackground)
        setPadding(dp2px(16))
        gravity = Gravity.LEFT or Gravity.CENTER
        textSizeInPx = dp2pxf(16f)
        setOnClickListener(this)
    }

    fun bindData(bookChapter: BookChapter?) {
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

    override fun onClick(v: View?) {
        val data = this.bookChapter ?: return

        onItemClick(data.order, data, this)
    }
}
