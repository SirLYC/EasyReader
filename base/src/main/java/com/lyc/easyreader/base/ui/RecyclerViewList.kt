package com.lyc.easyreader.base.ui

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.base.arch.FuncCallLiveEvent
import com.lyc.easyreader.base.arch.SingleLiveEvent

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class RecyclerViewList {
    val list = mutableListOf<Pair<Int, Any>>()
    val refreshListCall = FuncCallLiveEvent()
    val addItemCall = SingleLiveEvent<Int>()
    val removeItemCall = SingleLiveEvent<Int>()
    val changeItemCall = SingleLiveEvent<Int>()
    val moveItemCall = SingleLiveEvent<Pair<Int, Int>>()
    val rangeChangeItemCall = SingleLiveEvent<Triple<Int, Int, Any?>>()
    val rangeInsertItemCall = SingleLiveEvent<Pair<Int, Int>>()
    val rangeRemoveItemCall = SingleLiveEvent<Pair<Int, Int>>()

    fun registerAdapter(
        owner: LifecycleOwner,
        adapter: RecyclerView.Adapter<in RecyclerView.ViewHolder>
    ) {
        refreshListCall.observe(owner, Observer {
            adapter.notifyDataSetChanged()
        })
        addItemCall.observe(owner, Observer {
            adapter.notifyItemInserted(it)
        })
        removeItemCall.observe(owner, Observer {
            adapter.notifyItemRemoved(it)
        })
        changeItemCall.observe(owner, Observer {
            adapter.notifyItemChanged(it)
        })
        moveItemCall.observe(owner, Observer {
            adapter.notifyItemMoved(it.first, it.second)
        })
        rangeChangeItemCall.observe(owner, Observer {
            adapter.notifyItemRangeChanged(it.first, it.second, it.second)
        })
        rangeInsertItemCall.observe(owner, Observer {
            adapter.notifyItemRangeInserted(it.first, it.second)
        })
        rangeRemoveItemCall.observe(owner, Observer {
            adapter.notifyItemRangeRemoved(it.first, it.second)
        })
    }

}
