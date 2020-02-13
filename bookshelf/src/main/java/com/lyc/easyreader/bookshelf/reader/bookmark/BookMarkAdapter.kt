package com.lyc.easyreader.bookshelf.reader.bookmark

import android.R
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.api.book.BookMark
import com.lyc.easyreader.base.ui.getDrawableAttrRes
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.base.utils.rv.ReactiveAdapter

/**
 * Created by Liu Yuchuan on 2020/2/12.
 */
class BookMarkAdapter(
    list: ObservableList<BookMark>
) : ReactiveAdapter(list) {
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        viewType: Int,
        data: Any?,
        payloads: MutableList<Any>
    ) {
        (data as? BookMark)?.let { bookMark ->
            (holder.contentView as? BookMarkItemView)?.bindData(bookMark, position)
        }
    }

    override fun onCreateItemView(itemWrapper: FrameLayout, viewType: Int): View {
        itemWrapper.background = getDrawableAttrRes(R.attr.selectableItemBackground)
        return BookMarkItemView(itemWrapper.context).apply {
            layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
    }
}
