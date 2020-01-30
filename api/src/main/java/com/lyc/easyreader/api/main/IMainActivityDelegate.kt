package com.lyc.easyreader.api.main

import com.lyc.appinject.annotations.Service
import com.lyc.easyreader.base.utils.generateNewViewId

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
@Service
interface IMainActivityDelegate {
    companion object {
        val ID_BOOK_SHELF = generateNewViewId()
        val ID_USER_CENTER = generateNewViewId()
        val ID_DISCOVER = generateNewViewId()
    }

    fun addTabChangeListener(listener: ITabChangeListener)

    fun removeTabChangeListener(listener: ITabChangeListener)

    /**
     * @return 如果id不是tab id，则返回空串
     */
    fun tabIdToString(id: Int): String

    fun isMainTabId(id: Int): Boolean
}
