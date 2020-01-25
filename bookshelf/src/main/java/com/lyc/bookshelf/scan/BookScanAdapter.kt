package com.lyc.bookshelf.scan

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lyc.base.ui.theme.color_secondary_text
import com.lyc.base.utils.dp2pxf
import com.lyc.base.utils.rv.ReactiveAdapter
import com.lyc.bookshelf.VIEW_TYPE_EMPTY_ITEM
import com.lyc.bookshelf.VIEW_TYPE_SCAN_ITEM

/**
 * Created by Liu Yuchuan on 2020/1/24.
 */
class BookScanAdapter(
    viewModel: BookScanViewModel,
    private val positionSelectController: PositionSelectController
) : ReactiveAdapter(viewModel.list) {

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        viewType: Int,
        data: Any,
        payloads: MutableList<Any>
    ) {
        if (viewType == VIEW_TYPE_SCAN_ITEM) {
            (data as? BookScanItem)?.let { itemData ->
                (holder.itemView as? BookScanItemView)?.bindData(itemData, position)
            }
        }
    }

    override fun onCreateItemView(parent: ViewGroup, viewType: Int): View {
        return if (viewType == VIEW_TYPE_EMPTY_ITEM) {
            TextView(parent.context).apply {
                gravity = Gravity.CENTER
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(16f))
                text = "没有符合条件的文件~"
                setTextColor(color_secondary_text)
            }
        } else {
            BookScanItemView(parent.context, positionSelectController).apply {
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (holder.itemViewType == VIEW_TYPE_SCAN_ITEM) {
            (holder.itemView as? BookScanItemView)?.bindData(null, -1)
        }
    }
}
