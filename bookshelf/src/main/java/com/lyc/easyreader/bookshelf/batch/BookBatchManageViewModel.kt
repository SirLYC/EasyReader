package com.lyc.easyreader.bookshelf.batch

import androidx.lifecycle.ViewModel
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.bookshelf.BookManager
import com.lyc.easyreader.bookshelf.secret.SecretManager

/**
 * Created by Liu Yuchuan on 2020/3/11.
 */
class BookBatchManageViewModel : ViewModel() {
    val list: ObservableList<BookFile> = ObservableList()

    val checkedIds = arrayListOf<String>()

    fun deleteCheckedIds(): Boolean {
        val set = checkedIds.toSet()
        BookManager.instance.deleteBooks(set)
        list.removeAll { it.id in set }
        return list.isEmpty()
    }

    fun changeCheckIdsCollect(collect: Boolean) {
        val set = checkedIds.toSet()
        BookManager.instance.batchUpdateBookCollect(set, collect)
    }

    fun addCheckedIdsToSecret(): Boolean {
        val set = checkedIds.toSet()
        return SecretManager.addBooksToSecret(list.filter { set.contains(it.id) })
    }

    fun removeCheckedIdsFromSecret() {
        val set = checkedIds.toSet()
        BookManager.instance.removeBooksFromSecret(list.filter { set.contains(it.id) })
    }
}
