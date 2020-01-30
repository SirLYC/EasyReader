package com.lyc.easyreader.api.book

import android.net.Uri
import com.lyc.appinject.annotations.Service

/**
 * Created by Liu Yuchuan on 2020/1/27.
 */
@Service
interface IBookManager {

    fun importBooks(uriList: List<Uri>)

    fun addBookChangeListener(listener: IBookChangeListener)

    fun removeBookChangeListener(listener: IBookChangeListener)

    interface IBookChangeListener {
        fun onBooksImported(list: List<BookFile>)
    }
}
