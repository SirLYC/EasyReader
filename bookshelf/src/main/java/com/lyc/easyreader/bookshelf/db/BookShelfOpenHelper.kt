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

    private val bookFileRecordListenerHub =
        EventHubFactory.createDefault<IBookFileRecordListener>(true)

    private val bookFileCollectListenerHub =
        EventHubFactory.createDefault<IBookFileCollectListener>(true)

    interface IBookFileRecordListener {
        fun onBookReadRecordUpdate()
    }

    interface IBookFileCollectListener {
        fun onBookCollectChange(id: String)
    }

    fun addBookFileRecordListener(listener: IBookFileRecordListener) {
        bookFileRecordListenerHub.addEventListener(listener)
    }

    fun removeBookFileRecordListener(listener: IBookFileRecordListener) {
        bookFileRecordListenerHub.removeEventListener(listener)
    }

    fun addBookFileCollectListener(listener: IBookFileCollectListener) {
        bookFileCollectListenerHub.addEventListener(listener)
    }

    fun removeBookFileCollectListener(listener: IBookFileCollectListener) {
        bookFileCollectListenerHub.removeEventListener(listener)
    }

    fun asyncSaveUpdateBookAccess(bookFile: BookFile) {
        bookFile.lastAccessTime = System.currentTimeMillis()
        dbRunner.asyncRun(Runnable {
            daoSession.bookFileDao.insertOrReplace(bookFile)
            bookFileRecordListenerHub.getEventListeners().forEach {
                it.onBookReadRecordUpdate()
            }
        })
    }


    fun asyncUpdateBookReadRecord(bookReadRecord: BookReadRecord) {
        dbRunner.asyncRun(Runnable {
            daoSession.bookReadRecordDao.insertOrReplace(bookReadRecord)
            bookFileRecordListenerHub.getEventListeners().forEach {
                it.onBookReadRecordUpdate()
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
        sb.append(", j.${BookReadRecordDao.Properties.Desc.columnName}")
        sb.append(" from ${BookFileDao.TABLENAME} t left join ${BookReadRecordDao.TABLENAME} j on t.${BookFileDao.Properties.Id.columnName}=j.${BookReadRecordDao.Properties.BookId.columnName}")
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
                                ), bookFile
                            )
                        )
                    } while (it.moveToNext())
                }
            }

        } catch (e: Exception) {
            LogUtils.e(TAG, ex = e)
        }

        LogUtils.d(TAG, "Shelf book list=${list}")
        return list
    }

    fun queryBookCollect(bookFile: BookFile): BookCollect? {
        if (bookFile.id == null) {
            return null
        }
        return daoSession.bookCollectDao.load(bookFile.id)
    }

    fun updateBookCollect(bookCollect: BookCollect) {
        bookCollect.collectTime = System.currentTimeMillis()
        dbRunner.asyncRun(Runnable {
            daoSession.bookCollectDao.insertOrReplace(bookCollect)
            bookFileRecordListenerHub.getEventListeners().forEach {
                it.onBookReadRecordUpdate()
            }
        })
    }

    fun cancelBookCollect(id: String) {
        dbRunner.asyncRun(Runnable {

        })
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
                bookFileRecordListenerHub.getEventListeners().forEach {
                    it.onBookReadRecordUpdate()
                }
            }
        })
    }
}
