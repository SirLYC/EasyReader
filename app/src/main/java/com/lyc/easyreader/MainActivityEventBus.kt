package com.lyc.easyreader

import com.lyc.api.main.IMainActivityEventBus
import com.lyc.api.main.IMainActivityEventBus.Companion.ID_BOOK_SHELF
import com.lyc.api.main.IMainActivityEventBus.Companion.ID_DISCOVER
import com.lyc.api.main.IMainActivityEventBus.Companion.ID_USER_CENTER
import com.lyc.api.main.ITabChangeListener
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ServiceImpl
import com.lyc.common.EventHubFactory

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
@ServiceImpl(service = IMainActivityEventBus::class, createMethod = CreateMethod.GET_INSTANCE)
class MainActivityEventBus private constructor() : IMainActivityEventBus {

    companion object {
        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { MainActivityEventBus() }
    }

    private val eventHub = EventHubFactory.createDefault<ITabChangeListener>(true)

    override fun addTabChangeListener(listener: ITabChangeListener) {
        eventHub.addEventListener(listener)
    }

    override fun removeTabChangeListener(listener: ITabChangeListener) {
        eventHub.removeEventListener(listener)
    }

    override fun tabIdToString(id: Int) = when (id) {
        ID_BOOK_SHELF -> "书架"
        ID_DISCOVER -> "发现"
        ID_USER_CENTER -> "我的"
        else -> ""
    }

    fun notifyTabChange(tabId: Int) {
        eventHub.getEventListeners().forEach {
            it.onChangeToNewTab(tabId)
        }
    }

    fun notifyTabClick(tabId: Int) {
        eventHub.getEventListeners().forEach {
            it.onCurrentTabClick(tabId)
        }
    }
}
