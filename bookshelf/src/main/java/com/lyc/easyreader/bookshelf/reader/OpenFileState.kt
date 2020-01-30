package com.lyc.easyreader.bookshelf.reader

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
sealed class OpenFileState {
    object Querying : OpenFileState()
    object HandleChapter : OpenFileState()
    object Success : OpenFileState()
    class Error(val msg: String) : OpenFileState()
}
