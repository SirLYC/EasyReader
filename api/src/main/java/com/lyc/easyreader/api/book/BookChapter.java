package com.lyc.easyreader.api.book;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
@Entity
public class BookChapter {
    @Id
    private Long id;
    private int order;
    private long lastModified;
    @Index
    private long bookId;
    private String title;
    private long start;
    private long end;


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


    @Generated(hash = 1481387400)
    public BookChapter() {
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

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }


    public long getBookId() {
        return this.bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }


    public long getLastModified() {
        return this.lastModified;
    }


    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}