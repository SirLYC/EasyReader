package com.lyc.easyreader.bookshelf.reader.settings

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.bookshelf.reader.page.anim.PageAnimMode

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
class PageAnimModeAdapter : RecyclerView.Adapter<PageAnimModeAdapter.ViewHolder>() {

    private val values = PageAnimMode.values()

    class ViewHolder(val view: ReaderSettingsDialog.SelectButton) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        var data: PageAnimMode? = null

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            data?.let {
                if (it != ReaderSettings.instance.pageAnimMode.value) {
                    ReaderSettings.instance.pageAnimMode.value = it
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ReaderSettingsDialog.SelectButton(
                parent.context,
                ReaderSettings.currentPageStyle
            ).apply {
                layoutParams =
                    RecyclerView.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                        .apply { rightMargin = dp2px(16) }
            })
    }

    override fun getItemCount() = values.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < 0 || position >= values.size) {
            return
        }
        val data = values[position]
        holder.data = data
        holder.view.apply {
            updatePageStyle(ReaderSettings.currentPageStyle)
            text = data.displayName
            selectState = data == ReaderSettings.instance.pageAnimMode.value
        }
    }
}
