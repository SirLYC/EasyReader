package com.lyc.easyreader.bookshelf.reader.settings.colorpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.setPadding
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.ui.bottomsheet.BaseBottomSheet
import com.lyc.easyreader.base.utils.dp2px

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
class PageStyleColorPickerDialog : BaseBottomSheet() {
    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FrameLayout(ReaderApplication.appContext()).apply {
            setPadding(dp2px(50))
        }
    }
}
