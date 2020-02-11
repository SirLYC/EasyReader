package com.lyc.easyreader.bookshelf.db

import android.database.sqlite.SQLiteDatabase
import com.lyc.easyreader.api.book.DaoMaster
import org.greenrobot.greendao.database.Database

/**
 * Created by Liu Yuchuan on 2020/2/11.
 */
class MyDaoMaster : DaoMaster {
    constructor(db: SQLiteDatabase) : super(db)
    constructor(db: Database) : super(db)

    fun daoConfigMap() = this.daoConfigMap
}
