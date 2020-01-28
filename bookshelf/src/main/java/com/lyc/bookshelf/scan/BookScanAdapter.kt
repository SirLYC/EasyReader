package com.lyc.bookshelf.scan

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.lyc.base.ui.getDrawableRes
import com.lyc.base.ui.theme.color_accent
import com.lyc.base.utils.changeToColor
import com.lyc.base.utils.dp2px
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
            FrameLayout(parent.context).apply {
                addView(TextView(parent.context).apply {
                    setPadding(dp2px(16))
                    gravity = Gravity.CENTER

                    setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(16f))
                    text = "没有符合条件的文件~"
                    setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        getDrawableRes(com.lyc.api.R.drawable.ic_sleeping_cat)?.apply {
                            changeToColor(color_accent)
                        },
                        null,
                        null
                    )
                    compoundDrawablePadding = dp2px(32)
                    setTextColor(color_accent)
                }, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER
                })
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)
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
