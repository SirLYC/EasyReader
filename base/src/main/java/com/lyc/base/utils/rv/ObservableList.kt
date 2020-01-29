package com.lyc.base.utils.rv

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class ObservableList<T>(private val realList: MutableList<T>) : AbstractMutableList<T>(),
    ListUpdateCallbackExt {

    private val callbacks = mutableListOf<ListUpdateCallbackExt>()

    fun addCallback(callback: ListUpdateCallbackExt): Boolean = callbacks.add(callback)
    fun removeCallback(callback: ListUpdateCallbackExt): Boolean = callbacks.remove(callback)
    var enable = true

    override val size: Int
        get() = realList.size

    override fun get(index: Int): T = realList[index]

    override fun add(index: Int, element: T) {
        realList.add(index, element)
        this.onInserted(index, 1)
    }

    override fun set(index: Int, element: T): T {
        val result = realList.set(index, element)
        this.onChanged(index, 1, null)
        return result
    }

    override fun removeAt(index: Int): T {
        val result = realList.removeAt(index)
        this.onRemoved(index, 1)
        return result
    }

    fun changeItem(index: Int, payload: Any?) {
        this.onChanged(index, 1, payload)
    }

    /* ============================== batch notify ============================== */

    override fun addAll(elements: Collection<T>): Boolean {
        val oldSize = size
        val result = realList.addAll(elements) // return false if elements is Empty
        if (result) {
            this.onInserted(oldSize, elements.size)
        }
        return result
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val result = realList.addAll(index, elements) // return false if elements is Empty
        if (result) {
            this.onInserted(index, elements.size)
        }
        return result
    }

    override fun clear() {
        val oldSize = size
        if (oldSize == 0) {
            return
        }

        realList.clear()
        this.onRemoved(0, oldSize)
    }

    public override fun removeRange(fromIndex: Int, count: Int) {
        var remove = 0
        while (remove < count && fromIndex < size) {
            realList.removeAt(fromIndex)
            remove++
        }

        if (remove > 0) {
            this.onRemoved(fromIndex, remove)
        }
    }

    /* ============================== ListUpdateCallback ============================== */

    override fun onInserted(position: Int, count: Int) {
        if (enable) {
            callbacks.forEach { it.onInserted(position, count) }
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        if (enable) {
            callbacks.forEach { it.onRemoved(position, count) }
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        if (enable) {
            callbacks.forEach { it.onMoved(fromPosition, toPosition) }
        }
    }

    override fun onRefresh() {
        if (enable) {
            callbacks.forEach { it.onRefresh() }
        }
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        if (enable) {
            callbacks.forEach { it.onChanged(position, count, payload) }
        }
    }

    /* ============================== extension ============================== */

    fun replaceAll(elements: Collection<T>) {
        realList.clear()
        realList.addAll(elements)
        onRefresh()
    }

    fun replaceAll(singleElement: T) {
        realList.clear()
        realList.add(singleElement)
        onRefresh()
    }

    inline fun withoutCallback(func: ObservableList<T>.() -> Unit) {
        val enable = enable
        this.enable = false
        func()
        this.enable = enable
    }
}
