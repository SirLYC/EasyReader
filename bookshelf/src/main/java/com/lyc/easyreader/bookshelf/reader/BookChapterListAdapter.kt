package com.lyc.easyreader.bookshelf.reader

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.utils.rv.ReactiveAdapter

/**
 * Created by Liu Yuchuan on 2020/2/9.
 */
class BookChapterListAdapter(
    private val viewModel: ReaderViewModel,
    private val onItemClick: (pos: Int, bookChapter: BookChapter, view: View) -> Unit
) : ReactiveAdapter(viewModel.bookChapterList) {
    var reverse = false
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        viewType: Int,
        data: Any?,
        payloads: MutableList<Any>
    ) {
        val chapter = data as? BookChapter
        val view = holder.itemView as? ChapterItemView
        chapter?.let { view?.bindData(data, viewModel.currentChapter.value) }
    }

    override fun onCreateItemView(parent: ViewGroup, viewType: Int): View {
        return ChapterItemView(ReaderApplication.appContext(), onItemClick).apply {
            layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        (holder.itemView as? ChapterItemView)?.bindData(null, -1)
    }

    override fun getDataAt(position: Int): Any {
        if (reverse) {
            return list[list.size - position - 1]
        }
        return super.getDataAt(position)
    }
}
