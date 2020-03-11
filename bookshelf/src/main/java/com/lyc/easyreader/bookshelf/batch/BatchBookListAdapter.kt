package com.lyc.easyreader.bookshelf.batch

import android.graphics.Color
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.utils.buildCommonButtonBg
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.base.utils.rv.ReactiveAdapter
import com.lyc.easyreader.bookshelf.BookShelfItemView
import com.lyc.easyreader.bookshelf.db.BookShelfBook

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
        (holder.contentView as? BookShelfItemView)?.bindData((data as? BookFile)?.let {
            BookShelfBook(
                data,
                null,
                false
            )
        }, position)
    }

    override fun onCreateItemView(itemWrapper: FrameLayout, viewType: Int): View {
        itemWrapper.background = buildCommonButtonBg(Color.WHITE)
        return BookShelfItemView(itemWrapper.context).apply {
            layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        (holder.itemView as? BookShelfItemView)?.bindData(null, -1)
    }
}
