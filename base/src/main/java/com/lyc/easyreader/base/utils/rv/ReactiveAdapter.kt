package com.lyc.easyreader.base.utils.rv

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.base.R
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.theme.color_orange
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.base.utils.changeToColor
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf
import kotlin.math.abs

/**
 * Created by Liu Yuchuan on 2019/5/20.
 */
abstract class ReactiveAdapter(protected val list: ObservableList<out Any>) :
    RecyclerView.Adapter<ReactiveAdapter.ViewHolder>(),
    ListUpdateCallbackExt {
    companion object {
        const val PAYLOAD_EDIT_MODE_ANIM = 1
        const val PAYLOAD_EDIT_MODE_NO_ANIM = 4

        const val PAYLOAD_CHECK_CHANGE = 6

        const val PAYLOAD_LISTENER_CHANGE = 12
    }

    var editMode = false
        private set
    var itemClickListener: ItemClickListener? = null
        set(value) {
            if (field != value) {
                field = value
                notifyItemRangeChanged(0, itemCount, PAYLOAD_LISTENER_CHANGE)
            }
        }
    var itemCheckListener: ItemCheckListener? = null
    val checkAllLiveData = NonNullLiveData(false)
    private val checkedPos = hashSetOf<Int>()

    private val defaultEnterEditModeAnim by lazy {
        TranslateAnimation(
            Animation.RELATIVE_TO_SELF,
            -1f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.ABSOLUTE,
            0f,
            Animation.ABSOLUTE,
            0f
        ).apply {
            duration = 200
        }
    }

    private val defaultExitEditModeAnim by lazy {
        TranslateAnimation(
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            -1f,
            Animation.ABSOLUTE,
            0f,
            Animation.ABSOLUTE,
            0f
        ).apply {
            duration = 200
        }
    }

    protected open val enterEditModeAnimation: Animation = defaultEnterEditModeAnim

    protected open val exitEditModeAnimation: Animation = defaultExitEditModeAnim

    protected open val enterEditModeItemViewAnimation: Animation = TranslateAnimation(
        Animation.ABSOLUTE,
        -dp2pxf(40f),
        Animation.RELATIVE_TO_SELF,
        0f,
        Animation.ABSOLUTE,
        0f,
        Animation.ABSOLUTE,
        0f
    ).apply {
        duration = 200
    }

    protected open val exitEditModeItemViewAnimation: Animation = TranslateAnimation(
        Animation.ABSOLUTE,
        dp2pxf(40f),
        Animation.ABSOLUTE,
        0f,
        Animation.ABSOLUTE,
        0f,
        Animation.ABSOLUTE,
        0f
    ).apply {
        duration = 200
    }

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

    abstract fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        viewType: Int,
        data: Any?,
        payloads: MutableList<Any>
    )

    abstract fun onCreateItemView(parent: ViewGroup, viewType: Int): View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = FrameLayout(parent.context)
        val contentView = onCreateItemView(itemView, viewType)
        when (val childLp = contentView.layoutParams) {
            is RecyclerView.LayoutParams -> {
                itemView.layoutParams = childLp
                contentView.layoutParams = FrameLayout.LayoutParams(childLp)
            }
            is ViewGroup.MarginLayoutParams -> {
                itemView.layoutParams = RecyclerView.LayoutParams(childLp)
                contentView.layoutParams = FrameLayout.LayoutParams(childLp)
            }
            else -> {
                itemView.layoutParams = childLp
                contentView.layoutParams =
                    (childLp?.let { FrameLayout.LayoutParams(it) } ?: FrameLayout.LayoutParams(
                        MATCH_PARENT,
                        WRAP_CONTENT
                    ))
            }
        }
        itemView.addView(contentView)
        return ViewHolder(
            itemView,
            contentView
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
        val viewType = getItemViewType(position)
        val data = getDataAt(position)
        holder.listener = itemClickListener
        val canEnterEditMode = canEnterEditMode(position)
        LogUtils.d("AAA", "Payloads=${payloads}")
        var handled = false
        payloads.mapNotNull { it as? Int }.forEach { payload ->
            when {
                payload == PAYLOAD_LISTENER_CHANGE -> {
                    handled = true
                }
                payload == PAYLOAD_CHECK_CHANGE -> {
                    holder.checkBox?.let {
                        onBindCheckBox(
                            viewType,
                            position,
                            holder,
                            it,
                            checkedPos.contains(position)
                        )
                    }
                    handled = true
                }
                abs(payload).let { it == PAYLOAD_EDIT_MODE_ANIM || it == PAYLOAD_EDIT_MODE_NO_ANIM } && canEnterEditMode -> {
                    val editMode = payload > 0
                    val anim = abs(payload) == PAYLOAD_EDIT_MODE_ANIM
                    if (editMode != holder.editMode) {
                        holder.editMode = editMode
                        val checkBox = holder.checkBox ?: onCreateCheckBox(viewType, holder).also {
                            holder.checkBox = it
                        }
                        checkBox.isVisible = editMode
                        onBindEditModeContentViewLayout(
                            position,
                            viewType,
                            holder.contentView,
                            editMode
                        )
                        if (editMode && anim) {
                            checkBox.startAnimation(enterEditModeAnimation)
                            holder.contentView.startAnimation(enterEditModeItemViewAnimation)
                        } else if (!editMode && anim) {
                            checkBox.startAnimation(exitEditModeAnimation)
                            holder.contentView.startAnimation(exitEditModeItemViewAnimation)
                        }
                        if (canEnterEditMode && holder.editMode) {
                            holder.checkBox?.let {
                                onBindCheckBox(
                                    viewType,
                                    position,
                                    holder,
                                    it,
                                    false
                                )
                            }
                        }
                        handled = true
                    }
                }
            }
        }
        if (handled) {
            return
        }

        if (canEnterEditMode) {
            holder.editMode = editMode
            if (holder.editMode && holder.checkBox?.isVisible != true) {
                val checkBox = holder.checkBox ?: onCreateCheckBox(viewType, holder).also {
                    holder.checkBox = it
                }
                checkBox.isVisible = true
                onBindEditModeContentViewLayout(
                    position,
                    viewType,
                    holder.contentView,
                    true
                )
            } else if (!holder.editMode && holder.checkBox?.isVisible == true) {
                holder.checkBox?.isVisible = false
                onBindEditModeContentViewLayout(
                    position,
                    viewType,
                    holder.contentView,
                    false
                )
            }
            if (editMode) {
                holder.checkBox?.let {
                    onBindCheckBox(
                        viewType,
                        position,
                        holder,
                        it,
                        checkedPos.contains(position)
                    )
                }
            }
        }
        onBindViewHolder(holder, position, viewType, data, payloads)
    }

    protected open fun onBindEditModeContentViewLayout(
        position: Int,
        viewType: Int,
        contentView: View,
        enter: Boolean
    ) {
        (contentView.layoutParams as? FrameLayout.LayoutParams)?.apply {
            if (enter) {
                leftMargin += dp2px(40)
            } else {
                leftMargin -= dp2px(40)
            }
            gravity = Gravity.RIGHT
            contentView.layoutParams = this
        }
    }

    final override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        throw RuntimeException("Use onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) !")
    }

    protected open fun onCreateCheckBox(viewType: Int, holder: ViewHolder): View {
        val checkBox = DefaultCheckBox(holder.itemView.context)
        (holder.itemView as? FrameLayout)?.addView(
            checkBox,
            FrameLayout.LayoutParams(dp2px(40), dp2px(24)).apply {
                gravity = Gravity.LEFT or Gravity.CENTER
            })
        return checkBox
    }

    protected open fun onBindCheckBox(
        viewType: Int,
        pos: Int,
        holder: ViewHolder,
        checkBox: View,
        checked: Boolean
    ) {
        (checkBox as? DefaultCheckBox)?.checkState = checked
    }

    protected open fun getDataAt(position: Int): Any = list[position]

    fun toggleCheck(position: Int) {
        if (!editMode || position < 0 || position > itemCount || !canEnterEditMode(position)) {
            return
        }
        if (checkedPos.contains(position)) {
            uncheckPosition(position)
        } else {
            checkPosition(position)
        }
    }

    fun checkPosition(position: Int) {
        if (!editMode || position < 0 || position > itemCount || !canEnterEditMode(position)) {
            return
        }

        if (checkedPos.add(position)) {
            itemCheckListener?.onItemCheckChange(position, true)
            notifyItemChanged(position, PAYLOAD_CHECK_CHANGE)
            if (isCheckAll() && !checkAllLiveData.value) {
                checkAllLiveData.value = true
                itemCheckListener?.onItemCheckAllChange(true)
            }
        }
    }

    fun uncheckPosition(position: Int) {
        if (!editMode || position < 0 || position > itemCount || !canEnterEditMode(position)) {
            return
        }
        if (checkedPos.remove(position)) {
            itemCheckListener?.onItemCheckChange(position, false)
            notifyItemChanged(position, PAYLOAD_CHECK_CHANGE)
            if (checkAllLiveData.value) {
                checkAllLiveData.value = false
                itemCheckListener?.onItemCheckAllChange(false)
            }
        }
    }

    private fun isCheckAll(): Boolean {
        if (!editMode || itemCount <= 0) {
            return false
        }
        for (i in 0 until itemCount) {
            if (canEnterEditMode(i) && !checkedPos.contains(i)) {
                return false
            }
        }
        return true
    }

    fun checkAll() {
        for (i in 0 until itemCount) {
            if (canEnterEditMode(i)) {
                checkPosition(i)
            }
        }
    }

    fun uncheckAll() {
        for (i in 0 until itemCount) {
            if (canEnterEditMode(i)) {
                uncheckPosition(i)
            }
        }
    }

    fun checkCount(): Int {
        var count = 0
        for (i in 0 until itemCount) {
            if (canEnterEditMode(i) && checkedPos.contains(i)) {
                count++
            }
        }
        return count
    }

    protected open fun canEnterEditMode(position: Int) = true

    fun enterEditMode(anim: Boolean = true): Boolean {
        if (editMode) {
            return false
        }
        checkedPos.clear()
        editMode = true
        notifyItemRangeChanged(
            0,
            itemCount,
            if (anim) PAYLOAD_EDIT_MODE_ANIM else PAYLOAD_EDIT_MODE_NO_ANIM
        )
        return true
    }

    fun exitEditMode(anim: Boolean = true): Boolean {
        if (!editMode) {
            return false
        }
        checkedPos.clear()
        editMode = false
        notifyItemRangeChanged(
            0,
            itemCount,
            -(if (anim) PAYLOAD_EDIT_MODE_ANIM else PAYLOAD_EDIT_MODE_NO_ANIM)
        )
        return true
    }

    fun toggleEditMode(anim: Boolean = true): Boolean {
        return if (!editMode) {
            enterEditMode(anim)
        } else {
            exitEditMode(anim)
        }
    }

    fun toggleCheckAll() {
        if (checkAllLiveData.value) {
            uncheckAll()
        } else {
            checkAll()
        }
    }

    class ViewHolder(itemView: FrameLayout, val contentView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        var editMode = false
        var checkBox: View? = null
        var listener: ItemClickListener? = null

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            listener?.run {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(pos, v, editMode)
                }
            }
        }

        override fun onLongClick(v: View): Boolean {
            listener?.run {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    return onItemLongClick(pos, v, editMode)
                }
            }
            return false
        }
    }

    interface ItemClickListener {
        fun onItemClick(position: Int, view: View, editMode: Boolean)
        fun onItemLongClick(position: Int, view: View, editMode: Boolean): Boolean
    }

    interface ItemCheckListener {
        fun onItemCheckChange(position: Int, check: Boolean)
        fun onItemCheckAllChange(checkAll: Boolean)
    }

    private class DefaultCheckBox(context: Context?) : ImageView(context) {
        private val drawableChecked by lazy {
            getDrawableRes(R.drawable.ic_radio_button_checked_24dp)?.apply {
                changeToColor(color_orange)
            }
        }
        private val drawableUnchecked by lazy {
            getDrawableRes(R.drawable.ic_radio_button_unchecked_24dp)?.apply {
                changeToColor(color_orange)
            }
        }

        var checkState = false
            set(value) {
                if (field != value) {
                    field = value
                    if (value) {
                        setImageDrawable(drawableChecked)
                    } else {
                        setImageDrawable(drawableUnchecked)
                    }
                }
            }

        init {
            setPadding(dp2px(16), 0, 0, 0)
            scaleType = ScaleType.CENTER_INSIDE
            setImageDrawable(drawableUnchecked)
        }
    }
}
