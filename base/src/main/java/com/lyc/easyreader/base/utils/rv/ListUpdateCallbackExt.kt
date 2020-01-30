package com.lyc.easyreader.base.utils.rv

import androidx.recyclerview.widget.ListUpdateCallback

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
interface ListUpdateCallbackExt : ListUpdateCallback {
    fun onRefresh()
}
