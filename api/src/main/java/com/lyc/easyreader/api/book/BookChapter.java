package com.lyc.easyreader.api.book;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

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
    @NotNull
    private String bookId;
    private String title;
    private long start;
    private long end;
    // 区分是否是通过正则匹配到的
    private boolean realChapter;


    @Generated(hash = 1481387400)
    public BookChapter() {
    }


    @Generated(hash = 1356701021)
    public BookChapter(Long id, int order, long lastModified,
                       @NotNull String bookId, String title, long start, long end,
                       boolean realChapter) {
        this.id = id;
        this.order = order;
        this.lastModified = lastModified;
        this.bookId = bookId;
        this.title = title;
        this.start = start;
        this.end = end;
        this.realChapter = realChapter;
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


    public long getLastModified() {
        return this.lastModified;
    }


    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }


    public String getBookId() {
        return this.bookId;
    }


    public void setBookId(String bookId) {
        this.bookId = bookId;
    }


    public boolean getRealChapter() {
        return this.realChapter;
    }


    public void setRealChapter(boolean realChapter) {
        this.realChapter = realChapter;
    }
}
