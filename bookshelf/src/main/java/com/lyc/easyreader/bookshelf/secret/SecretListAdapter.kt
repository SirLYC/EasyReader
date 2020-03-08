package com.lyc.easyreader.bookshelf.secret

import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.bookshelf.BookShelfListAdapter
import com.lyc.easyreader.bookshelf.db.BookShelfBook

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
class SecretListAdapter(list: ObservableList<BookShelfBook>) : BookShelfListAdapter(list) {
    override fun canEnterEditMode(position: Int): Boolean {
        return false
    }
}
