package com.lyc.easyreader.bookshelf.db

import com.lyc.easyreader.api.book.BookFile

/**
 * Created by Liu Yuchuan on 2020/2/11.
 */
class BookShelfBook(
    bookFile: BookFile,
    @JvmField
    val recordDesc: String?,
    @JvmField
    var collect: Boolean
) : BookFile(bookFile)
