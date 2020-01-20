package com.lyc.bookshelf

import com.lyc.api.main.AbstractMainTab
import com.lyc.api.main.IMainActivityEventBus
import com.lyc.api.main.IMainTab
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImpl

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@ExtensionImpl(extension = IMainTab::class, createMethod = CreateMethod.GET_INSTANCE)
class BookShelfMainTab : AbstractMainTab<BookShelfFragment>(), IMainTab {
    companion object {
        @JvmStatic
        val instance by lazy { BookShelfMainTab() }
    }

    override fun getId() = IMainActivityEventBus.ID_BOOK_SHELF
    override fun newFragmentInstance() = BookShelfFragment()
    override fun getFragmentClass() = BookShelfFragment::class.java
}
