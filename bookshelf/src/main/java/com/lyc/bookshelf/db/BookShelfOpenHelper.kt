package com.lyc.bookshelf.db

import android.util.Log
import com.lyc.api.book.BookFile
import com.lyc.api.book.BookFileDao
import com.lyc.api.book.DaoMaster
import com.lyc.base.ReaderApplication
import com.lyc.common.thread.SingleThreadRunner

/**
 * Created by Liu Yuchuan on 2020/1/26.
 */
class BookShelfOpenHelper private constructor() :
    DaoMaster.OpenHelper(ReaderApplication.appContext(), "BookShelf") {
    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { BookShelfOpenHelper() }

        private const val TAG = "BookShelfOpenHelper"
    }

    private val daoMaster by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { DaoMaster(writableDb) }

    private val daoSession by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { daoMaster.newSession() }

    private val dbRunner = SingleThreadRunner("BookShelf-DB")

    fun insertBookFile(bookFile: BookFile) {
        dbRunner.awaitRun(Runnable {
            daoSession.bookFileDao.insert(bookFile)
        })
    }

    fun insertOrReplaceBookFile(bookFile: BookFile) {
        dbRunner.awaitRun(Runnable {
            daoSession.bookFileDao.insertOrReplace(bookFile)
        })
    }

    fun setBookFileStatus(id: Long, status: BookFile.Status) {
        val db = daoSession.database
        dbRunner.awaitRun(Runnable {
            val sql =
                "update ${BookFileDao.TABLENAME} set ${BookFileDao.Properties.Status.columnName}=${status.name} where ${BookFileDao.Properties.Id.columnName}=${id}"
            Log.d(TAG, "setBookFileStatus, sql=$sql")
            db.execSQL(sql)
        })
    }
}
