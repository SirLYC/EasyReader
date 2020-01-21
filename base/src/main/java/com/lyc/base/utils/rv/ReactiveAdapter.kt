package com.lyc.base.utils.rv

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Liu Yuchuan on 2019/5/20.
 */
abstract class ReactiveAdapter(protected val list: ObservableList<Pair<Int, Any>>) :
    RecyclerView.Adapter<ReactiveAdapter.ViewHolder>(),
    ListUpdateCallbackExt {

    override fun onInserted(position: Int, count: Int) {
        if (count == 1) {
            notifyItemInserted(position)
        } else {
            notifyItemRangeInserted(position, count)
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        if (count == 1) {
            notifyItemRemoved(position)
        } else {
            notifyItemRangeRemoved(position, count)
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRefresh() {
        notifyDataSetChanged()
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) =
        if (count == 1) {
            notifyItemChanged(position, payload)
        } else {
            notifyItemRangeChanged(position, count, payload)
        }

    fun observe(activity: Activity) {
        activity.application.registerActivityLifecycleCallbacks(ReactiveActivityRegistry(activity))
        list.addCallback(this)
    }

    fun observe(fragment: Fragment) {
        val fm = fragment.fragmentManager
        if (fm != null) {
            fm.registerFragmentLifecycleCallbacks(ReactiveFragmentRegistry(fragment), false)
            list.addCallback(this)
        }
    }

    inner class ReactiveActivityRegistry(
        private val activity: Activity
    ) : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivityDestroyed(activity: Activity) {
            if (this.activity == activity) {
                list.removeCallback(this@ReactiveAdapter)
                activity.application.unregisterActivityLifecycleCallbacks(this)
            }
        }
    }

    inner class ReactiveFragmentRegistry(
        private val fragment: Fragment
    ) : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            super.onFragmentViewDestroyed(fm, f)
            if (fragment == f) {
                list.removeCallback(this@ReactiveAdapter)
                fm.unregisterFragmentLifecycleCallbacks(this)
            }
        }
    }

    override fun getItemCount() = list.size

    override fun getItemViewType(position: Int): Int {
        if (position < 0 || position >= list.size) {
            return -1
        }

        return list[position].first
    }

    abstract fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        viewType: Int,
        data: Any,
        payloads: MutableList<Any>
    )

    abstract fun onCreateItemView(parent: ViewGroup, viewType: Int): View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            onCreateItemView(
                parent,
                viewType
            )
        )
    }

    final override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (position < 0 || position >= list.size) {
            return
        }
        val pair = list[position]
        onBindViewHolder(holder, position, pair.first, pair.second, payloads)
    }

    final override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        throw RuntimeException("Use onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) !")
    }

    class ViewHolder(itemVIew: View) : RecyclerView.ViewHolder(itemVIew)
}
