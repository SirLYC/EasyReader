package com.lyc.easyreader.api.book;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.lyc.easyreader.api.book.BookFile.CharsetConverter;
import com.lyc.easyreader.api.book.BookFile.StatusConverter;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

import java.nio.charset.Charset;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "BOOK_FILE".
 */
public class BookFileDao extends AbstractDao<BookFile, Long> {

    public static final String TABLENAME = "BOOK_FILE";

    public BookFileDao(DaoConfig config) {
        super(config);
    }

    private final CharsetConverter charsetConverter = new CharsetConverter();
    private final StatusConverter statusConverter = new StatusConverter();

    /**
     * Creates the underlying database table.
     */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"BOOK_FILE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"REAL_PATH\" TEXT," + // 1: realPath
                "\"FILENAME\" TEXT," + // 2: filename
                "\"FILE_EXT\" TEXT," + // 3: fileExt
                "\"IMPORT_TIME\" INTEGER NOT NULL ," + // 4: importTime
                "\"LAST_ACCESS_TIME\" INTEGER NOT NULL ," + // 5: lastAccessTime
                "\"DELETE_TIME\" INTEGER NOT NULL ," + // 6: deleteTime
                "\"HANDLE_CHAPTER_LAST_MODIFIED\" INTEGER NOT NULL ," + // 7: handleChapterLastModified
                "\"CHARSET\" TEXT," + // 8: charset
                "\"STATUS\" TEXT NOT NULL );"); // 9: status
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_BOOK_FILE_FILENAME ON \"BOOK_FILE\"" +
                " (\"FILENAME\" ASC);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_BOOK_FILE_IMPORT_TIME_DESC ON \"BOOK_FILE\"" +
                " (\"IMPORT_TIME\" DESC);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_BOOK_FILE_LAST_ACCESS_TIME_DESC ON \"BOOK_FILE\"" +
                " (\"LAST_ACCESS_TIME\" DESC);");
    }

    public BookFileDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /**
     * Drops the underlying database table.
     */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"BOOK_FILE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, BookFile entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }

        String realPath = entity.getRealPath();
        if (realPath != null) {
            stmt.bindString(2, realPath);
        }

        String filename = entity.getFilename();
        if (filename != null) {
            stmt.bindString(3, filename);
        }

        String fileExt = entity.getFileExt();
        if (fileExt != null) {
            stmt.bindString(4, fileExt);
        }
        stmt.bindLong(5, entity.getImportTime());
        stmt.bindLong(6, entity.getLastAccessTime());
        stmt.bindLong(7, entity.getDeleteTime());
        stmt.bindLong(8, entity.getHandleChapterLastModified());

        Charset charset = entity.getCharset();
        if (charset != null) {
            stmt.bindString(9, charsetConverter.convertToDatabaseValue(charset));
        }
        stmt.bindString(10, statusConverter.convertToDatabaseValue(entity.getStatus()));
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, BookFile entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }

        String realPath = entity.getRealPath();
        if (realPath != null) {
            stmt.bindString(2, realPath);
        }

        String filename = entity.getFilename();
        if (filename != null) {
            stmt.bindString(3, filename);
        }

        String fileExt = entity.getFileExt();
        if (fileExt != null) {
            stmt.bindString(4, fileExt);
        }
        stmt.bindLong(5, entity.getImportTime());
        stmt.bindLong(6, entity.getLastAccessTime());
        stmt.bindLong(7, entity.getDeleteTime());
        stmt.bindLong(8, entity.getHandleChapterLastModified());

        Charset charset = entity.getCharset();
        if (charset != null) {
            stmt.bindString(9, charsetConverter.convertToDatabaseValue(charset));
        }
        stmt.bindString(10, statusConverter.convertToDatabaseValue(entity.getStatus()));
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }

    @Override
    public BookFile readEntity(Cursor cursor, int offset) {
        BookFile entity = new BookFile( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // realPath
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // filename
                cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // fileExt
                cursor.getLong(offset + 4), // importTime
                cursor.getLong(offset + 5), // lastAccessTime
                cursor.getLong(offset + 6), // deleteTime
                cursor.getLong(offset + 7), // handleChapterLastModified
                cursor.isNull(offset + 8) ? null : charsetConverter.convertToEntityProperty(cursor.getString(offset + 8)), // charset
                statusConverter.convertToEntityProperty(cursor.getString(offset + 9)) // status
        );
        return entity;
    }

    @Override
    public void readEntity(Cursor cursor, BookFile entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setRealPath(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setFilename(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setFileExt(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setImportTime(cursor.getLong(offset + 4));
        entity.setLastAccessTime(cursor.getLong(offset + 5));
        entity.setDeleteTime(cursor.getLong(offset + 6));
        entity.setHandleChapterLastModified(cursor.getLong(offset + 7));
        entity.setCharset(cursor.isNull(offset + 8) ? null : charsetConverter.convertToEntityProperty(cursor.getString(offset + 8)));
        entity.setStatus(statusConverter.convertToEntityProperty(cursor.getString(offset + 9)));
    }
     
    /**
     * Properties of entity BookFile.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property RealPath = new Property(1, String.class, "realPath", false, "REAL_PATH");
        public final static Property Filename = new Property(2, String.class, "filename", false, "FILENAME");
        public final static Property FileExt = new Property(3, String.class, "fileExt", false, "FILE_EXT");
        public final static Property ImportTime = new Property(4, long.class, "importTime", false, "IMPORT_TIME");
        public final static Property LastAccessTime = new Property(5, long.class, "lastAccessTime", false, "LAST_ACCESS_TIME");
        public final static Property DeleteTime = new Property(6, long.class, "deleteTime", false, "DELETE_TIME");
        public final static Property HandleChapterLastModified = new Property(7, long.class, "handleChapterLastModified", false, "HANDLE_CHAPTER_LAST_MODIFIED");
        public final static Property Charset = new Property(8, String.class, "charset", false, "CHARSET");
        public final static Property Status = new Property(9, String.class, "status", false, "STATUS");
    }
    
    @Override
    protected final Long updateKeyAfterInsert(BookFile entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(BookFile entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(BookFile entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
