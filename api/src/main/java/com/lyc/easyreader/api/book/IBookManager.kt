package com.lyc.easyreader.api.book

import android.net.Uri
import com.lyc.appinject.annotations.InjectApi

/**
 * Created by Liu Yuchuan on 2020/1/27.
 */
@InjectApi
interface IBookManager {

    fun importBooks(uriList: List<Uri>)

    fun importBookAndOpen(uri: Uri)

    fun deleteBook(id: String, async: Boolean = true)

    fun deleteBooks(ids: Iterable<String>, async: Boolean = true)

    fun updateBookCollect(id: String, collect: Boolean, async: Boolean = true)

    fun batchUpdateBookCollect(ids: Iterable<String>, collect: Boolean, async: Boolean = true)

    fun addBookChangeListener(listener: IBookChangeListener)

    fun removeBookChangeListener(listener: IBookChangeListener)

    fun alterBookName(id: String, newName: String, async: Boolean = true)

    fun shareBookFile(bookFile: BookFile)

    fun addBooksToSecret(bookFiles: Iterable<BookFile>, async: Boolean = true)

    fun removeBooksFromSecret(bookFiles: Iterable<BookFile>, async: Boolean = true)

    interface IBookChangeListener {

        fun onBooksImported(list: List<BookFile>) = Unit

        fun onSecretBooksChanged() = Unit

        fun onBookDeleted() = Unit

        fun onBookBatchChange() = Unit

        fun onBookCollectChange(id: String, collect: Boolean) = Unit

        fun onBookInfoUpdate(id: String) = Unit
    }
}
