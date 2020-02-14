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

    fun deleteBooks(ids: Iterable<String>, async: Boolean = true)

    fun updateBookCollect(id: String, collect: Boolean, async: Boolean = true)

    fun addBookChangeListener(listener: IBookChangeListener)

    fun removeBookChangeListener(listener: IBookChangeListener)

    fun alterBookName(id: String, newName: String, async: Boolean = true)

    fun shareBookFile(bookFile: BookFile)

    fun openBookFileByOther(bookFile: BookFile)

    interface IBookChangeListener {

        fun onBooksImported(list: List<BookFile>)

        fun onBookDeleted()

        fun onBookCollectChange(id: String, collect: Boolean)

        fun onBookInfoUpdate(id: String)
    }
}
