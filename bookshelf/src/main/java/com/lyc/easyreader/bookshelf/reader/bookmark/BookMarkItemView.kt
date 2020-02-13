package com.lyc.easyreader.bookshelf.reader.bookmark

import android.content.Context
import android.text.TextUtils
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.lyc.easyreader.api.book.BookMark
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.textSizeInDp
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettings
import com.lyc.easyreader.bookshelf.utils.detailTimeString

/**
 * Created by Liu Yuchuan on 2020/2/12.
 */
class BookMarkItemView(
    context: Context
) : LinearLayout(context) {
    private val chapterTv = TextView(context)
    private val descTv = TextView(context)
    private val fontColor = ReaderSettings.currentPageStyle.fontColor

    private var pos = 0
    private var bookMark: BookMark? = null

    init {
        orientation = VERTICAL
        setPadding(dp2px(16))
        initView()
    }

    private fun initView() {
        chapterTv.setTextColor(fontColor)
        chapterTv.textSizeInDp = 16f
        addView(chapterTv, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        descTv.setPadding(0, dp2px(8), 0, 0)
        descTv.setTextColor(fontColor and 0xB2FFFFFF.toInt())
        descTv.textSizeInDp = 14f
        descTv.maxLines = 1
        descTv.isSingleLine = true
        descTv.ellipsize = TextUtils.TruncateAt.END
        addView(descTv, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    fun bindData(bookMark: BookMark?, pos: Int) {
        if (this.bookMark == bookMark && this.pos == pos) {
            return
        }
        this.bookMark = bookMark
        this.pos = pos

        if (bookMark != null) {
            chapterTv.text =
                if (!TextUtils.isEmpty(bookMark.title)) bookMark.title else "第${bookMark.chapter}章"
            descTv.text =
                ("${bookMark.time.detailTimeString()}${if (!TextUtils.isEmpty(bookMark.desc)) "  " + bookMark.desc else ""}")
        }
    }
}
