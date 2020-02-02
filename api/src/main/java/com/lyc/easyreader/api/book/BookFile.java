package com.lyc.easyreader.api.book;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.nio.charset.Charset;

/**
 * Created by Liu Yuchuan on 2020/1/26.
 */
@Entity(indexes = {
        @Index(value = "filename"),
        @Index(value = "importTime desc"),
        @Index(value = "lastAccessTime desc")
})
public class BookFile implements Parcelable {

    @Id
    private Long id;
    private String realPath;
    private String filename;
    private String fileExt;
    private long importTime;
    private long lastAccessTime;
    private long deleteTime;
    public static final Creator<BookFile> CREATOR = new Creator<BookFile>() {
        @Override
        public BookFile createFromParcel(Parcel in) {
            return new BookFile(in);
        }

        @Override
        public BookFile[] newArray(int size) {
            return new BookFile[size];
        }
    };
    private long handleChapterLastModified;
    @Convert(converter = StatusConverter.class, columnType = String.class)
    private Status status;
    @Convert(converter = CharsetConverter.class, columnType = String.class)
    private Charset charset;


    @Generated(hash = 453179158)
    public BookFile(Long id, String realPath, String filename, String fileExt,
                    long importTime, long lastAccessTime, long deleteTime,
                    long handleChapterLastModified, Status status, Charset charset) {
        this.id = id;
        this.realPath = realPath;
        this.filename = filename;
        this.fileExt = fileExt;
        this.importTime = importTime;
        this.lastAccessTime = lastAccessTime;
        this.deleteTime = deleteTime;
        this.handleChapterLastModified = handleChapterLastModified;
        this.status = status;
        this.charset = charset;
    }


    @Generated(hash = 1858747483)
    public BookFile() {
    }


    protected BookFile(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        realPath = in.readString();
        filename = in.readString();
        fileExt = in.readString();
        importTime = in.readLong();
        lastAccessTime = in.readLong();
        deleteTime = in.readLong();
        handleChapterLastModified = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(realPath);
        dest.writeString(filename);
        dest.writeString(fileExt);
        dest.writeLong(importTime);
        dest.writeLong(lastAccessTime);
        dest.writeLong(deleteTime);
        dest.writeLong(handleChapterLastModified);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getHandleChapterLastModified() {
        return handleChapterLastModified;
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

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
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

    public void setHandleChapterLastModified(long handleChapterLastModified) {
        this.handleChapterLastModified = handleChapterLastModified;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public static class CharsetConverter implements PropertyConverter<Charset, String> {

        @Override
        public Charset convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return null;
            }
            return Charset.forName(databaseValue);
        }

        @Override
        public String convertToDatabaseValue(Charset entityProperty) {
            if (entityProperty == null) {
                return null;
            }
            return entityProperty.name();
        }
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
