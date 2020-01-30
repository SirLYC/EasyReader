package  com.lyc.easyreader.bookshelf

import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImpl
import com.lyc.easyreader.api.R
import com.lyc.easyreader.api.main.AbstractMainTabDelegate
import com.lyc.easyreader.api.main.IMainActivityDelegate
import com.lyc.easyreader.api.main.IMainTabDelegate

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
