package com.lyc.easyreader.api.book

import android.net.Uri
import com.lyc.appinject.annotations.Service

/**
 * Created by Liu Yuchuan on 2020/1/27.
 */
@Service
interface IBookManager {

    fun importBooks(uriList: List<Uri>)

    fun deleteBook(id: String, async: Boolean = true)

    fun updateBookCollect(id: String, collect: Boolean, async: Boolean = true)

    fun addBookChangeListener(listener: IBookChangeListener)

    fun removeBookChangeListener(listener: IBookChangeListener)

    fun alterBookName(id: String, newName: String, async: Boolean = true)

    interface IBookChangeListener {

        fun onBooksImported(list: List<BookFile>)

        fun onBookDeleted(id: String)

        fun onBookCollectChange(id: String, collect: Boolean)

        fun onBookInfoUpdate(id: String)
    }
}
