package com.lyc.api.book;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import java.util.Map;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 *
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig bookFileDaoConfig;

    private final BookFileDao bookFileDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        bookFileDaoConfig = daoConfigMap.get(BookFileDao.class).clone();
        bookFileDaoConfig.initIdentityScope(type);

        bookFileDao = new BookFileDao(bookFileDaoConfig, this);

        registerDao(BookFile.class, bookFileDao);
    }

    public void clear() {
        bookFileDaoConfig.clearIdentityScope();
    }

    public BookFileDao getBookFileDao() {
        return bookFileDao;
    }

}
