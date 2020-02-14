package com.lyc.easyreader.bookshelf.db

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.lyc.common.EventHubFactory
import com.lyc.common.thread.ExecutorFactory
import com.lyc.common.thread.SingleThreadRunner
import com.lyc.easyreader.api.book.*
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.utils.LogUtils
import org.greenrobot.greendao.internal.SqlUtils
import java.io.File

/**
 * Created by Liu Yuchuan on 2020/1/26.
 */
class BookShelfOpenHelper private constructor() :
    DaoMaster.OpenHelper(ReaderApplication.appContext(), "BookShelf") {
    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { BookShelfOpenHelper() }

        private const val TAG = "BookShelfOpenHelper"
    }

    private val daoMaster by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { MyDaoMaster(writableDb) }

    private val daoSession by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { daoMaster.newSession() }

    private val dbRunner = SingleThreadRunner("BookShelf-DB")

    private val bookFileInfoUpdateListenerHub =
        EventHubFactory.createDefault<IBookFileInfoUpdateListener>(true)

    private val bookMarkChangeListenerHub =
        EventHubFactory.createDefault<IBookMarkChangeListener>(true)

    interface IBookFileInfoUpdateListener {
        fun onBookInfoUpdate()
    }

    interface IBookMarkChangeListener {
        fun onBookMarkChange(bookId: String)
    }

    fun addBookFileInfoUpdateListener(listener: IBookFileInfoUpdateListener) {
        bookFileInfoUpdateListenerHub.addEventListener(listener)
    }

    fun removeBookFileInfoUpdateListener(listener: IBookFileInfoUpdateListener) {
        bookFileInfoUpdateListenerHub.removeEventListener(listener)
    }

    fun addBookMarkChangeListener(listener: IBookMarkChangeListener) {
        bookMarkChangeListenerHub.addEventListener(listener)
    }

    fun removeBookMarkChangeListener(listener: IBookMarkChangeListener) {
        bookMarkChangeListenerHub.removeEventListener(listener)
    }

    fun asyncSaveUpdateBookAccess(bookFile: BookFile) {
        bookFile.lastAccessTime = System.currentTimeMillis()
        dbRunner.asyncRun(Runnable {
            daoSession.bookFileDao.insertOrReplace(bookFile)
            bookFileInfoUpdateListenerHub.getEventListeners().forEach {
                it.onBookInfoUpdate()
            }
        })
    }

    fun loadBookMarks(bookId: String): List<BookMark> {
        return daoSession.bookMarkDao.queryBuilder()
            .where(BookMarkDao.Properties.BookId.eq(bookId))
            .orderDesc(BookMarkDao.Properties.Time, BookMarkDao.Properties.Chapter)
            .list()
    }

    fun addBookMark(bookMark: BookMark) {
        if (bookMark.bookId == null) {
            return
        }
        dbRunner.asyncRun(Runnable {
            daoSession.bookMarkDao.insert(bookMark)
            bookMarkChangeListenerHub.getEventListeners().forEach {
                it.onBookMarkChange(bookMark.bookId)
            }
        })
    }

    fun deleteBookMark(bookMark: BookMark) {
        dbRunner.asyncRun(Runnable {
            daoSession.bookMarkDao.delete(bookMark)
            bookMarkChangeListenerHub.getEventListeners().forEach {
                it.onBookMarkChange(bookMark.bookId)
            }
        })
    }

    fun deleteAllReadRecord(callback: () -> Unit = {}) {
        dbRunner.asyncRun(Runnable {
            daoSession.bookReadRecordDao.deleteAll()
            writableDatabase.execSQL("update ${BookFileDao.TABLENAME} set ${BookFileDao.Properties.LastAccessTime.columnName}=0")
            callback()
            bookFileInfoUpdateListenerHub.getEventListeners().forEach {
                it.onBookInfoUpdate()
            }
        })
    }

    fun deleteBookMarksFor(bookId: String) {
        dbRunner.asyncRun(Runnable {
            daoSession.bookMarkDao.queryBuilder()
                .where(BookMarkDao.Properties.BookId.eq(bookId))
                .buildDelete()
                .forCurrentThread()
                .executeDeleteWithoutDetachingEntities()
            bookMarkChangeListenerHub.getEventListeners().forEach {
                it.onBookMarkChange(bookId)
            }
        })
    }


    fun asyncUpdateBookReadRecord(bookReadRecord: BookReadRecord) {
        dbRunner.asyncRun(Runnable {
            daoSession.bookReadRecordDao.insertOrReplace(bookReadRecord)
            bookFileInfoUpdateListenerHub.getEventListeners().forEach {
                it.onBookInfoUpdate()
            }
        })
    }

    fun loadBookRecordOrNew(bookFile: BookFile): BookReadRecord {
        return daoSession.bookReadRecordDao.load(bookFile.id) ?: BookReadRecord(
            bookFile.id,
            0,
            -1,
            -1,
            0,
            null
        )
    }

    fun reloadBookFile(bookFile: BookFile): BookFile? {
        if (bookFile.id == null) {
            return null
        }

        return daoSession.bookFileDao.load(bookFile.id)
    }

    fun alterBookName(
        id: String,
        newName: String,
        async: Boolean = true,
        callback: () -> Unit = {}
    ) {
        val db = daoSession.database
        val command = Runnable {
            val sql =
                "update ${BookFileDao.TABLENAME} set ${BookFileDao.Properties.Filename.columnName}=\"$newName\" where ${BookFileDao.Properties.Id.columnName}=\"$id\""
            Log.d(TAG, "alterBookName, sql=$sql")
            db.execSQL(sql)
            callback()
        }
        if (async) {
            dbRunner.asyncRun(command)
        } else {
            dbRunner.awaitRun(command)
        }
    }

    fun deleteBookFile(id: String, async: Boolean = true, callback: () -> Unit = {}) {
        val command = Runnable {
            daoSession.runInTx {
                deleteOneBook(id)
                callback()
            }
        }
        if (async) {
            dbRunner.asyncRun(command)
        } else {
            dbRunner.awaitRun(command)
        }
    }

    fun deleteBookFiles(ids: Iterable<String>, async: Boolean = true, callback: () -> Unit = {}) {
        val command = Runnable {
            daoSession.runInTx {
                ids.forEach {
                    deleteOneBook(it)
                }
                callback()
            }
        }
        if (async) {
            dbRunner.asyncRun(command)
        } else {
            dbRunner.awaitRun(command)
        }
    }

    private fun deleteOneBook(id: String) {
        daoSession.bookFileDao.load(id)?.run {
            File(realPath).delete()
        }
        daoSession.bookMarkDao.queryBuilder()
            .where(BookMarkDao.Properties.BookId.eq(id))
            .buildDelete()
            .forCurrentThread()
            .executeDeleteWithoutDetachingEntities()
        daoSession.bookReadRecordDao.deleteByKey(id)
        daoSession.bookCollectDao.deleteByKey(id)
        daoSession.bookChapterDao.queryBuilder()
            .where(BookChapterDao.Properties.BookId.eq(id))
            .buildDelete()
            .forCurrentThread()
            .executeDeleteWithoutDetachingEntities()
        daoSession.bookFileDao.deleteByKey(id)
    }

    /**
     * @return false表示id重复
     */
    fun insertBookFile(bookFile: BookFile): Boolean {
        var success = false
        dbRunner.awaitRun(Runnable {
            try {
                daoSession.bookFileDao.insert(bookFile)
                success = true
            } catch (e: SQLiteConstraintException) {
                // ignore
            }
        })
        return success
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

    @Deprecated("Use loadBookShelfBookList")
    fun loadBookShelfList(): List<BookFile> {
        return daoSession.bookFileDao.queryBuilder()
            .where(
                BookFileDao.Properties.RealPath.isNotNull,
                BookFileDao.Properties.Status.eq(BookFile.Status.NORMAL.name)
            )
            .orderDesc(BookFileDao.Properties.LastAccessTime, BookFileDao.Properties.ImportTime)
            .list()
    }

    fun loadBookShelfBookList(): MutableList<BookShelfBook> {
        val sb = StringBuilder("select ")
        SqlUtils.appendColumns(
            sb,
            "t",
            daoMaster.daoConfigMap()[BookFileDao::class.java]!!.allColumns
        )
        sb.append(", j1.${BookReadRecordDao.Properties.Desc.columnName}, j2.${BookCollectDao.Properties.BookId.columnName}")
        sb.append(" from ${BookFileDao.TABLENAME} t left join ${BookReadRecordDao.TABLENAME} j1 on t.${BookFileDao.Properties.Id.columnName}=j1.${BookReadRecordDao.Properties.BookId.columnName} left join ${BookCollectDao.TABLENAME} j2 on t.${BookFileDao.Properties.Id.columnName}=j2.${BookCollectDao.Properties.BookId.columnName}")
            .append(" where t.${BookFileDao.Properties.Status.columnName}=\"${BookFile.Status.NORMAL.name}\"")
            .append(" order by ${BookFileDao.Properties.LastAccessTime.columnName} desc, ${BookFileDao.Properties.ImportTime.columnName} desc")
        Log.d(TAG, "sql=$sb")

        val list = mutableListOf<BookShelfBook>()
        try {
            writableDatabase.rawQuery(sb.toString(), null).use {
                val bookFileOffset = 0
                val recordOffset =
                    daoMaster.daoConfigMap()[BookFileDao::class.java]!!.allColumns.size
                if (it.moveToFirst()) {
                    do {
                        val bookFile = daoSession.bookFileDao.readEntity(
                            it,
                            bookFileOffset
                        )
                        list.add(
                            BookShelfBook(
                                if (it.isNull(recordOffset)) null else it.getString(
                                    recordOffset
                                ), it.getShort(recordOffset + 1).toInt() != 0, bookFile
                            )
                        )
                    } while (it.moveToNext())
                }
            }

        } catch (e: Exception) {
            LogUtils.e(TAG, ex = e)
        }

        return list
    }

    fun loadCollectBookList(): MutableList<BookShelfBook> {
        val sb = StringBuilder("select ")
        SqlUtils.appendColumns(
            sb,
            "t",
            daoMaster.daoConfigMap()[BookFileDao::class.java]!!.allColumns
        )
        sb.append(", j1.${BookReadRecordDao.Properties.Desc.columnName}, j2.${BookCollectDao.Properties.BookId.columnName}")
        sb.append(" from ${BookFileDao.TABLENAME} t left join ${BookReadRecordDao.TABLENAME} j1 on t.${BookFileDao.Properties.Id.columnName}=j1.${BookReadRecordDao.Properties.BookId.columnName} left join ${BookCollectDao.TABLENAME} j2 on t.${BookFileDao.Properties.Id.columnName}=j2.${BookCollectDao.Properties.BookId.columnName}")
            .append(" where t.${BookFileDao.Properties.Status.columnName}=\"${BookFile.Status.NORMAL.name}\" and j2.${BookCollectDao.Properties.Collected.columnName} != 0 and j2.${BookCollectDao.Properties.Collected.columnName} is not null")
            .append(" order by ${BookCollectDao.Properties.CollectTime.columnName} desc")
        Log.d(TAG, "loadCollectBookList sql=$sb")

        val list = mutableListOf<BookShelfBook>()
        try {
            writableDatabase.rawQuery(sb.toString(), null).use {
                val bookFileOffset = 0
                val recordOffset =
                    daoMaster.daoConfigMap()[BookFileDao::class.java]!!.allColumns.size
                if (it.moveToFirst()) {
                    do {
                        val bookFile = daoSession.bookFileDao.readEntity(
                            it,
                            bookFileOffset
                        )
                        list.add(
                            BookShelfBook(
                                if (it.isNull(recordOffset)) null else it.getString(
                                    recordOffset
                                ), true, bookFile
                            )
                        )
                    } while (it.moveToNext())
                }
            }

        } catch (e: Exception) {
            LogUtils.e(TAG, ex = e)
        }

        return list
    }

    fun queryBookCollect(bookFile: BookFile): BookCollect? {
        if (bookFile.id == null) {
            return null
        }
        return daoSession.bookCollectDao.load(bookFile.id)
    }

    fun updateBookCollect(
        id: String,
        collect: Boolean,
        async: Boolean = true,
        callback: () -> Unit = {}
    ) {
        val time = System.currentTimeMillis()
        val command = Runnable {
            if (collect) {
                daoSession.insertOrReplace(BookCollect(id, true, time))
            } else {
                writableDatabase.execSQL("update ${BookCollectDao.TABLENAME} set ${BookCollectDao.Properties.Collected.columnName}=0 where ${BookCollectDao.Properties.BookId.columnName}=\"${id}\"")
            }
            callback()
        }
        if (async) {
            dbRunner.asyncRun(command)
        } else {
            dbRunner.awaitRun(command)
        }
    }

    fun queryBookChapters(bookFile: BookFile): List<BookChapter>? {
        if (bookFile.id == null) {
            return null
        }

        if (bookFile.handleChapterLastModified <= 0 || bookFile.charset == null) {

            // 传入的entity可能不是数据库中最新的，要用id查一下
            val bookFileInDb = daoSession.bookFileDao.load(bookFile.id)
            bookFile.handleChapterLastModified = bookFileInDb.handleChapterLastModified
            bookFile.charset = bookFileInDb.charset

            if (bookFile.handleChapterLastModified <= 0 || bookFile.charset == null) {
                return null
            }
        }

        val file = File(bookFile.realPath)
        val lastModified = file.lastModified()
        if (lastModified != bookFile.handleChapterLastModified) {
            return null
        }

        return daoSession.bookChapterDao.queryBuilder()
            .where(
                BookChapterDao.Properties.BookId.eq(bookFile.id),
                BookChapterDao.Properties.LastModified.eq(lastModified)
            )
            .orderDesc()
            .list()
    }

    fun saveBookChapters(bookFile: BookFile, list: List<BookChapter>) {
        dbRunner.awaitRun(Runnable {
            daoSession.runInTx {
                daoSession.bookChapterDao.queryBuilder()
                    .where(BookChapterDao.Properties.BookId.eq(bookFile.id))
                    .buildDelete()
                    .forCurrentThread()
                    .executeDeleteWithoutDetachingEntities()
                daoSession.bookFileDao.insertOrReplace(bookFile)
                daoSession.bookChapterDao.saveInTx(list)
            }
            ExecutorFactory.CPU_BOUND_EXECUTOR.execute {
                bookFileInfoUpdateListenerHub.getEventListeners().forEach {
                    it.onBookInfoUpdate()
                }
            }
        })
    }
}
