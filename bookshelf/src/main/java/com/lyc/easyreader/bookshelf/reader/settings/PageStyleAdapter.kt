package com.lyc.easyreader.bookshelf.reader.settings

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.bookshelf.reader.page.PageStyle

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
class PageStyleAdapter : RecyclerView.Adapter<PageStyleAdapter.ViewHolder>() {

    private val list = ArrayList(PageStyle.innerStyles.values)

    class ViewHolder(val view: PageStyleItemView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PageStyleItemView(parent.context).apply {
            layoutParams =
                RecyclerView.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    rightMargin = dp2px(16)
                }
        })
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < 0 || position >= list.size) {
            return
        }
        holder.view.bindData(list[position])
    }
}
