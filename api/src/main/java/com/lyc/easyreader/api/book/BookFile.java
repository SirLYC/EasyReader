package com.lyc.easyreader.api.book;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
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

    private String realPath;
    private String filename;
    private String fileExt;
    private long importTime;
    private long lastAccessTime;
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
    private int lastChapter;
    private int lastPageInChapter;
    private long deleteTime;
    private long handleChapterLastModified;
    @Convert(converter = CharsetConverter.class, columnType = String.class)
    private Charset charset;
    @Convert(converter = StatusConverter.class, columnType = String.class)
    @NotNull
    private Status status;
    @Id
    private String id;
    private String lastChapterDesc;

    @Generated(hash = 637696708)
    public BookFile(String realPath, String filename, String fileExt, long importTime, long lastAccessTime,
                    int lastChapter, int lastPageInChapter, long deleteTime, long handleChapterLastModified,
                    Charset charset, @NotNull Status status, String id, String lastChapterDesc) {
        this.realPath = realPath;
        this.filename = filename;
        this.fileExt = fileExt;
        this.importTime = importTime;
        this.lastAccessTime = lastAccessTime;
        this.lastChapter = lastChapter;
        this.lastPageInChapter = lastPageInChapter;
        this.deleteTime = deleteTime;
        this.handleChapterLastModified = handleChapterLastModified;
        this.charset = charset;
        this.status = status;
        this.id = id;
        this.lastChapterDesc = lastChapterDesc;
    }

    @Generated(hash = 1858747483)
    public BookFile() {
    }

    protected BookFile(Parcel in) {
        realPath = in.readString();
        filename = in.readString();
        fileExt = in.readString();
        importTime = in.readLong();
        lastAccessTime = in.readLong();
        lastChapter = in.readInt();
        lastPageInChapter = in.readInt();
        deleteTime = in.readLong();
        handleChapterLastModified = in.readLong();
        id = in.readString();
        status = Status.valueOf(in.readString());
        if (in.readByte() == 0) {
            charset = null;
        } else {
            charset = Charset.forName(in.readString());
        }
    }

    public void set(BookFile other) {
        this.realPath = other.realPath;
        this.filename = other.filename;
        this.fileExt = other.fileExt;
        this.importTime = other.importTime;
        this.lastAccessTime = other.lastAccessTime;
        this.lastChapter = other.lastChapter;
        this.lastPageInChapter = other.lastPageInChapter;
        this.deleteTime = other.deleteTime;
        this.handleChapterLastModified = other.handleChapterLastModified;
        this.charset = other.charset;
        this.status = other.status;
        this.id = other.id;
        this.lastChapterDesc = other.lastChapterDesc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(realPath);
        dest.writeString(filename);
        dest.writeString(fileExt);
        dest.writeLong(importTime);
        dest.writeLong(lastAccessTime);
        dest.writeInt(lastChapter);
        dest.writeInt(lastPageInChapter);
        dest.writeLong(deleteTime);
        dest.writeLong(handleChapterLastModified);
        dest.writeString(id);
        dest.writeString(status.name());
        if (charset == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(charset.name());
        }
    }

    public long getHandleChapterLastModified() {
        return handleChapterLastModified;
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

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLastChapter() {
        return this.lastChapter;
    }

    public void setLastChapter(int lastChapter) {
        this.lastChapter = lastChapter;
    }

    public int getLastPageInChapter() {
        return this.lastPageInChapter;
    }

    public void setLastPageInChapter(int lastPageInChapter) {
        this.lastPageInChapter = lastPageInChapter;
    }

    public String getLastChapterDesc() {
        return this.lastChapterDesc;
    }

    public void setLastChapterDesc(String lastChapterDesc) {
        this.lastChapterDesc = lastChapterDesc;
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
