package com.lyc.easyreader

import android.util.SparseArray
import androidx.core.util.contains
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.InjectApiImpl
import com.lyc.common.EventHubFactory
import com.lyc.easyreader.api.main.IMainActivityDelegate
import com.lyc.easyreader.api.main.IMainTabDelegate
import com.lyc.easyreader.api.main.ITabChangeListener
import com.lyc.easyreader.base.getOneToManyApiList

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
@InjectApiImpl(api = IMainActivityDelegate::class, createMethod = CreateMethod.GET_INSTANCE)
class MainActivityDelegate private constructor() : IMainActivityDelegate {
    private val id2NameMap = SparseArray<String>()

    companion object {
        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { MainActivityDelegate() }
    }

    init {
        for (tabDelegate in getOneToManyApiList<IMainTabDelegate>()) {
            id2NameMap.put(tabDelegate.getId(), tabDelegate.getName())
        }
    }

    private val eventHub = EventHubFactory.createDefault<ITabChangeListener>(true)

    override fun addTabChangeListener(listener: ITabChangeListener) {
        eventHub.addEventListener(listener)
    }

    override fun removeTabChangeListener(listener: ITabChangeListener) {
        eventHub.removeEventListener(listener)
    }

    override fun tabIdToString(id: Int) = id2NameMap[id] ?: ""

    override fun isMainTabId(id: Int) = id.let { id2NameMap.contains(id) }

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
