package com.lyc.easyreader.bookshelf.db

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.lyc.common.EventHubFactory
import com.lyc.common.thread.ExecutorFactory
import com.lyc.common.thread.SingleThreadRunner
import com.lyc.easyreader.api.book.*
import com.lyc.easyreader.base.ReaderApplication
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

    private val daoMaster by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { DaoMaster(writableDb) }

    private val daoSession by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { daoMaster.newSession() }

    private val dbRunner = SingleThreadRunner("BookShelf-DB")

    private val bookFileRecordListenerHub =
        EventHubFactory.createDefault<IBookFileRecordListener>(true)

    interface IBookFileRecordListener {
        fun onBookFileRecordUpdate(bookFile: BookFile)
    }

    fun addBookFileRecordListener(listener: IBookFileRecordListener) {
        bookFileRecordListenerHub.addEventListener(listener)
    }

    fun removeBookFileRecordListener(listener: IBookFileRecordListener) {
        bookFileRecordListenerHub.removeEventListener(listener)
    }

    fun asyncSaveUpdateBookAccess(bookFile: BookFile) {
        bookFile.lastAccessTime = System.currentTimeMillis()
        dbRunner.asyncRun(Runnable {
            daoSession.bookFileDao.insertOrReplace(bookFile)
            bookFileRecordListenerHub.getEventListeners().forEach {
                it.onBookFileRecordUpdate(bookFile)
            }
        })
    }


    fun asyncSaveUpdateBookRecord(bookFile: BookFile, chapter: Int, page: Int, desc: String) {
        if (bookFile.lastChapter == chapter && bookFile.lastPageInChapter == page) {
            return
        }
        bookFile.lastChapter = chapter
        bookFile.lastPageInChapter = page
        bookFile.lastChapterDesc = desc
        dbRunner.asyncRun(Runnable {
            daoSession.bookFileDao.insertOrReplace(bookFile)
            bookFileRecordListenerHub.getEventListeners().forEach {
                it.onBookFileRecordUpdate(bookFile)
            }
        })
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

    fun loadBookShelfList(): List<BookFile> {
        return daoSession.bookFileDao.queryBuilder()
            .where(
                BookFileDao.Properties.RealPath.isNotNull,
                BookFileDao.Properties.Status.eq(BookFile.Status.NORMAL.name)
            )
            .orderDesc(BookFileDao.Properties.LastAccessTime, BookFileDao.Properties.ImportTime)
            .list()
    }

    fun queryBookCollect(bookFile: BookFile): BookCollect? {
        if (bookFile.id == null) {
            return null
        }
        return daoSession.bookCollectDao.load(bookFile.id)
    }

    fun updateBookCollect(bookCollect: BookCollect) {
        dbRunner.asyncRun(Runnable {
            daoSession.bookCollectDao.insertOrReplace(bookCollect)
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
                    it.onBookFileRecordUpdate(bookFile)
                }
            }
        })
    }
}
