package com.lyc.easyreader.bookshelf.batch

import android.graphics.Color
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.base.utils.rv.ReactiveAdapter

/**
 * Created by Liu Yuchuan on 2020/1/28.
 */
open class BatchBookListAdapter(
    list: ObservableList<BookFile>
) : ReactiveAdapter(list) {
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        viewType: Int,

        data: Any?,
        payloads: MutableList<Any>
    ) {
        (holder.contentView as? BookBatchItemView)?.bindData(data as? BookFile, position)
    }

    override fun onCreateItemView(itemWrapper: FrameLayout, viewType: Int): View {
        itemWrapper.setBackgroundColor(Color.WHITE)
        return BookBatchItemView(itemWrapper.context).apply {
            layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        (holder.itemView as? BookBatchItemView)?.bindData(null, -1)
    }
}
