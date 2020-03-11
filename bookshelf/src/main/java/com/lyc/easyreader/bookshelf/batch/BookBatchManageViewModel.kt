package com.lyc.easyreader.bookshelf.batch

import androidx.lifecycle.ViewModel
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.utils.rv.ObservableList
import com.lyc.easyreader.bookshelf.BookManager

/**
 * Created by Liu Yuchuan on 2020/3/11.
 */
class BookBatchManageViewModel : ViewModel() {

    var deleteIfRemoveCollect = false
    var deleteIfRemoveSecret = true
    var deleteIfAddSecret = true
    val list: ObservableList<BookFile> = ObservableList()
    val checkedIds = arrayListOf<String>()

    fun deleteCheckedIds(): Boolean {
        val set = checkedIds.toSet()
        BookManager.instance.deleteBooks(set)
        list.removeAll { it.id in set }
        return list.isEmpty()
    }

    fun changeCheckIdsCollect(collect: Boolean): Boolean {
        val set = checkedIds.toSet()
        BookManager.instance.batchUpdateBookCollect(set, collect)
        if (deleteIfRemoveCollect && !collect) {
            list.removeAll { it.id in set }
        }
        return list.isEmpty()
    }

    fun removeCheckedIdsFromSecret(): Boolean {
        val set = checkedIds.toSet()
        BookManager.instance.removeBooksFromSecret(list.filter { set.contains(it.id) })
        list.removeAll { it.id in set }
        return list.isEmpty()
    }
}
