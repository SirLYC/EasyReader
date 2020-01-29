package com.lyc.bookshelf

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.lyc.api.book.BookFile
import com.lyc.base.utils.rv.ObservableList
import com.lyc.base.utils.rv.ReactiveAdapter

/**
 * Created by Liu Yuchuan on 2020/1/28.
 */
class BookShelfListAdapter(
    list: ObservableList<Pair<Int, Any>>,
    private val onItemClickListener: OnItemClickListener
) : ReactiveAdapter(list) {
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        viewType: Int,
        data: Any,
        payloads: MutableList<Any>
    ) {
        (holder.itemView as? BookShelfItemView)?.bindData(data as? BookFile, position)
    }

    override fun onCreateItemView(parent: ViewGroup, viewType: Int): View {
        return BookShelfItemView(parent.context, onItemClickListener).apply {
            layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        (holder.itemView as? BookShelfItemView)?.bindData(null, -1)
    }

    interface OnItemClickListener {
        fun onBookShelfItemClick(pos: Int, data: BookFile, view: BookShelfItemView)
    }
}
