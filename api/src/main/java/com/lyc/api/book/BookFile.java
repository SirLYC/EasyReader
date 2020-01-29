package com.lyc.api.book;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.converter.PropertyConverter;

/**
 * Created by Liu Yuchuan on 2020/1/26.
 */
@Entity(indexes = {
        @Index(value = "filename", unique = true),
        @Index(value = "importTime desc"),
        @Index(value = "lastAccessTime desc")
}, generateGettersSetters = false)
public class BookFile {

    @Id
    private Long id;
    private String realPath;
    private String filename;
    private long importTime;
    private long lastAccessTime;
    private long deleteTime;
    @Convert(converter = StatusConverter.class, columnType = String.class)
    private Status status;

    @Generated(hash = 1858747483)
    public BookFile() {
    }

    @Generated(hash = 838807339)
    public BookFile(Long id, String realPath, String filename, long importTime,
                    long lastAccessTime, long deleteTime, Status status) {
        this.id = id;
        this.realPath = realPath;
        this.filename = filename;
        this.importTime = importTime;
        this.lastAccessTime = lastAccessTime;
        this.deleteTime = deleteTime;
        this.status = status;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRealPath() {
        return this.realPath;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getImportTime() {
        return importTime;
    }

    public void setImportTime(long importTime) {
        this.importTime = importTime;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        TMP,
        NORMAL
    }

    public static class StatusConverter implements PropertyConverter<Status, String> {

        @Override
        public Status convertToEntityProperty(String databaseValue) {
            return Status.valueOf(databaseValue);
        }

        @Override
        public String convertToDatabaseValue(Status entityProperty) {
            return entityProperty.name();
        }
    }
}