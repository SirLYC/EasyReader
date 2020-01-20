package com.lyc.bookshelf

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lyc.api.main.AbstractMainTabFragment

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class BookShelfFragment : AbstractMainTabFragment() {
    override fun newViewInstance(container: ViewGroup?): View? {
        return TextView(container?.context).apply {
            gravity = Gravity.CENTER
            text = "书架"
        }
    }
}
