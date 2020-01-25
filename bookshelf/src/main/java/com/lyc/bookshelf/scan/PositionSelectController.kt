package com.lyc.bookshelf.scan

import com.lyc.common.EventHubFactory
import java.util.*

/**
 * Created by Liu Yuchuan on 2020/1/24.
 */
class PositionSelectController {
    private val selectedPositions = LinkedHashSet<Int>()
    private val eventHub = EventHubFactory.createDefault<PositionSelectListener>(false)

    fun changeSelectState(position: Int, select: Boolean): Boolean {
        val result = if (select) {
            selectedPositions.add(position)
        } else {
            selectedPositions.remove(position)
        }

        if (result) {
            eventHub.getEventListeners().forEach {
                it.onPositionSelect(position, select)
            }
        }

        return result
    }

    fun isSelectAll(totalCnt: Int) = totalCnt == selectedPositions.size

    fun selectAll(totalCnt: Int) {
        if (selectedPositions.size == totalCnt) {
            return
        }

        selectedPositions.addAll(0 until totalCnt)
        eventHub.getEventListeners().forEach {
            it.onSelectAllChange(true)
        }
    }

    fun unSelectAll() {
        if (selectedPositions.isNotEmpty()) {
            selectedPositions.clear()
            eventHub.getEventListeners().forEach {
                it.onSelectAllChange(false)
            }
        }
    }

    fun addListener(listener: PositionSelectListener) {
        eventHub.addEventListener(listener)
    }

    fun removeListener(listener: PositionSelectListener) {
        eventHub.removeEventListener(listener)
    }

    fun flipSelectState(position: Int) {

        if (selectContains(position)) {
            changeSelectState(position, false)
        } else {
            changeSelectState(position, true)
        }
    }

    fun selectContains(position: Int): Boolean {
        return selectedPositions.contains(position)
    }

    fun selectCount() = selectedPositions.size

    interface PositionSelectListener {
        fun onPositionSelect(position: Int, select: Boolean)

        fun onSelectAllChange(select: Boolean)
    }
}
