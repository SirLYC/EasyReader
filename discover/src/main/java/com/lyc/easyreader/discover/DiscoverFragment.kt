package com.lyc.easyreader.discover

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lyc.easyreader.api.main.AbstractMainTabFragment

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class DiscoverFragment : AbstractMainTabFragment() {
    override fun newViewInstance(container: ViewGroup?): View? {
        return TextView(container?.context).apply {
            gravity = Gravity.CENTER
            text = "发现"
        }
    }
}
