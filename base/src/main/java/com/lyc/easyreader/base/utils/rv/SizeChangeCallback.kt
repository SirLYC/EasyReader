package com.lyc.easyreader.base.utils.rv

/**
 * Created by Liu Yuchuan on 2020/2/13.
 */
interface SizeChangeCallback : ListUpdateCallbackExt {
    override fun onRefresh() = listSizeUpdate()
    override fun onChanged(position: Int, count: Int, payload: Any?) = Unit
    override fun onMoved(fromPosition: Int, toPosition: Int) = Unit
    override fun onInserted(position: Int, count: Int) = listSizeUpdate()
    override fun onRemoved(position: Int, count: Int) = listSizeUpdate()

    fun listSizeUpdate() {

    }
}
