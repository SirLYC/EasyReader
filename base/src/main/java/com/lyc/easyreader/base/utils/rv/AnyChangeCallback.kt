package com.lyc.easyreader.base.utils.rv

/**
 * Created by Liu Yuchuan on 2020/2/13.
 */
interface AnyChangeCallback : ListUpdateCallbackExt {
    override fun onRefresh() = anyListUpdate()
    override fun onChanged(position: Int, count: Int, payload: Any?) = anyListUpdate()
    override fun onMoved(fromPosition: Int, toPosition: Int) = anyListUpdate()
    override fun onInserted(position: Int, count: Int) = anyListUpdate()
    override fun onRemoved(position: Int, count: Int) = anyListUpdate()

    fun anyListUpdate() {

    }
}
