package com.lyc.easyreader.bookshelf.reader.settings

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.base.utils.buildCommonButtonBg
import com.lyc.easyreader.base.utils.buildCommonButtonTextColor
import com.lyc.easyreader.base.utils.dp2px

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
class OrientationAdapter : RecyclerView.Adapter<OrientationAdapter.ViewHolder>() {

    private val values = ScreenOrientation.values()

    class ViewHolder(val view: ReaderSettingsDialog.SelectButton) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        var data: ScreenOrientation? = null

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            data?.let {
                if (it != ReaderSettings.instance.screenOrientation.value) {
                    ReaderSettings.instance.screenOrientation.value = it
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ReaderSettingsDialog.SelectButton(
                parent.context,
                buildCommonButtonBg(ReaderSettingsDialog.contentColor, true),
                buildCommonButtonBg(ReaderSettingsDialog.selectColor, true),
                buildCommonButtonTextColor(ReaderSettingsDialog.contentColor),
                buildCommonButtonTextColor(ReaderSettingsDialog.selectColor)
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
            text = data.displayName
            selectState = data == ReaderSettings.instance.screenOrientation.value
        }
    }
}
