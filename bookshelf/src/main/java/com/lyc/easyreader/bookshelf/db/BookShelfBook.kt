package com.lyc.easyreader.bookshelf.db

import com.lyc.easyreader.api.book.BookFile

/**
 * Created by Liu Yuchuan on 2020/2/11.
 */
class BookShelfBook(
    val recordDesc: String?,
    var collect: Boolean,
    bookFile: BookFile
) : BookFile(bookFile)
