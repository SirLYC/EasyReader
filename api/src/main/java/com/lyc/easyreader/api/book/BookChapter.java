package com.lyc.easyreader.api.book;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
@Entity(indexes = {@Index(value = "bookId")})
public class BookChapter implements Parcelable {
    @Id
    private Long id;
    private int order;
    private long lastModified;
    @Index
    private long bookId;
    private String title;
    private long start;
    private long end;


    @Generated(hash = 1481387400)
    public BookChapter() {
    }

    @Generated(hash = 1730275621)
    public BookChapter(Long id, int order, long lastModified, long bookId,
                       String title, long start, long end) {
        this.id = id;
        this.order = order;
        this.lastModified = lastModified;
        this.bookId = bookId;
        this.title = title;
        this.start = start;
        this.end = end;
    }

    public static final Creator<BookChapter> CREATOR = new Creator<BookChapter>() {
        @Override
        public BookChapter createFromParcel(Parcel in) {
            return new BookChapter(in);
        }

        @Override
        public BookChapter[] newArray(int size) {
            return new BookChapter[size];
        }
    };

    protected BookChapter(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        order = in.readInt();
        lastModified = in.readLong();
        bookId = in.readLong();
        title = in.readString();
        start = in.readLong();
        end = in.readLong();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getBookId() {
        return this.bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeInt(order);
        dest.writeLong(lastModified);
        dest.writeLong(bookId);
        dest.writeString(title);
        dest.writeLong(start);
        dest.writeLong(end);
    }
}
