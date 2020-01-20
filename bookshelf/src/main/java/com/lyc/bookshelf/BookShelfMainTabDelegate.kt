package com.lyc.bookshelf

import com.lyc.api.R
import com.lyc.api.main.AbstractMainTabDelegate
import com.lyc.api.main.IMainActivityDelegate
import com.lyc.api.main.IMainTabDelegate
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImpl

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@ExtensionImpl(extension = IMainTabDelegate::class, createMethod = CreateMethod.GET_INSTANCE)
class BookShelfMainTabDelegate : AbstractMainTabDelegate<BookShelfFragment>(), IMainTabDelegate {
    companion object {
        @JvmStatic
        val instance by lazy { BookShelfMainTabDelegate() }
    }

    override fun getIconDrawableResId() = R.drawable.ic_library_books_24dp
    override fun getOrder() = 1
    override fun getId() = IMainActivityDelegate.ID_BOOK_SHELF
    override fun getName() = "书架"
    override fun newFragmentInstance() = BookShelfFragment()
    override fun getFragmentClass() = BookShelfFragment::class.java
}
