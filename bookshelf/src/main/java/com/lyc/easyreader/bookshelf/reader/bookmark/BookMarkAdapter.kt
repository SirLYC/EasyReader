package com.lyc.easyreader.bookshelf.reader.bookmark

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.api.book.BookMark
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.base.utils.rv.ReactiveAdapter

/**
 * Created by Liu Yuchuan on 2020/2/12.
 */
class BookMarkAdapter(
    list: ObservableList<BookMark>,
    private val onItemClick: (Int, View) -> Unit,
    private val onItemLongClick: (Int, View) -> Unit
) : ReactiveAdapter(list) {
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        viewType: Int,
        data: Any?,
        payloads: MutableList<Any>
    ) {
        (data as? BookMark)?.let { bookMark ->
            (holder.itemView as? BookMarkItemView)?.bindData(bookMark, position)
        }
    }

    override fun onCreateItemView(parent: ViewGroup, viewType: Int): View {
        return BookMarkItemView(parent.context, onItemClick, onItemLongClick).apply {
            layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
    }
}
