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
    private boolean isSecret;
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
    private long deleteTime;
    private long handleChapterLastModified;
    @Convert(converter = CharsetConverter.class, columnType = String.class)
    private Charset charset;
    @Convert(converter = StatusConverter.class, columnType = String.class)
    @NotNull
    private Status status;
    @Id
    private String id;

    @Generated(hash = 449745737)
    public BookFile(String realPath, String filename, String fileExt, boolean isSecret, long importTime,
                    long lastAccessTime, long deleteTime, long handleChapterLastModified, Charset charset, @NotNull Status status,
                    String id) {
        this.realPath = realPath;
        this.filename = filename;
        this.fileExt = fileExt;
        this.isSecret = isSecret;
        this.importTime = importTime;
        this.lastAccessTime = lastAccessTime;
        this.deleteTime = deleteTime;
        this.handleChapterLastModified = handleChapterLastModified;
        this.charset = charset;
        this.status = status;
        this.id = id;
    }

    @Generated(hash = 1858747483)
    public BookFile() {
    }

    public BookFile(BookFile other) {
        this.realPath = other.realPath;
        this.filename = other.filename;
        this.fileExt = other.fileExt;
        this.isSecret = other.isSecret;
        this.importTime = other.importTime;
        this.lastAccessTime = other.lastAccessTime;
        this.deleteTime = other.deleteTime;
        this.handleChapterLastModified = other.handleChapterLastModified;
        this.charset = other.charset;
        this.status = other.status;
        this.id = other.id;
    }

    protected BookFile(Parcel in) {
        realPath = in.readString();
        filename = in.readString();
        fileExt = in.readString();
        isSecret = in.readByte() != 0;
        importTime = in.readLong();
        lastAccessTime = in.readLong();
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
        this.isSecret = other.isSecret;
        this.importTime = other.importTime;
        this.lastAccessTime = other.lastAccessTime;
        this.deleteTime = other.deleteTime;
        this.handleChapterLastModified = other.handleChapterLastModified;
        this.charset = other.charset;
        this.status = other.status;
        this.id = other.id;
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
        dest.writeByte(isSecret ? (byte) 1 : (byte) 0);
        dest.writeLong(importTime);
        dest.writeLong(lastAccessTime);
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

    public boolean getIsSecret() {
        return this.isSecret;
    }

    public void setIsSecret(boolean isSecret) {
        this.isSecret = isSecret;
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
