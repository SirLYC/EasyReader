package com.lyc.bookshelf

import android.net.Uri
import com.lyc.base.ReaderApplication
import com.lyc.base.arch.repo.IRepository

/**
 * Created by Liu Yuchuan on 2020/1/26.
 */
class BookFilesRepository : IRepository {
    companion object {
        const val IMPORT_PROGRESS_FACTOR_OPEN = 1
        const val IMPORT_PROGRESS_FACTOR_COPY = 10
    }

    private fun importBookFile(uri: Uri, bookImportProgressListener: BookImportProgressListener?) {
        try {
            ReaderApplication.appContext().contentResolver.openInputStream(uri)
        } catch (e: Exception) {

        }
    }

    interface BookImportProgressListener {
        fun onProgressUpdate(obj: BookImportProgressObj)
    }

    data class BookImportProgressObj(
        val progress: Int,
        val total: Int
    )
}
