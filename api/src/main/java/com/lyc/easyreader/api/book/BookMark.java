package com.lyc.easyreader.api.book;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by Liu Yuchuan on 2020/2/11.
 */
@Entity
public class BookMark {
    @Id
    private Long id;
    @NotNull
    private String bookId;
    private int chapter;
    private int offsetStart;
    private int offsetEnd;
    private int page;
    private long time;
    private String title;
    private String desc;

    @Generated(hash = 104815026)
    public BookMark(Long id, @NotNull String bookId, int chapter, int offsetStart,
                    int offsetEnd, int page, long time, String title, String desc) {
        this.id = id;
        this.bookId = bookId;
        this.chapter = chapter;
        this.offsetStart = offsetStart;
        this.offsetEnd = offsetEnd;
        this.page = page;
        this.time = time;
        this.title = title;
        this.desc = desc;
    }

    @Generated(hash = 1704575762)
    public BookMark() {
    }

    public String getBookId() {
        return this.bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getChapter() {
        return this.chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public int getOffsetStart() {
        return this.offsetStart;
    }

    public void setOffsetStart(int offsetStart) {
        this.offsetStart = offsetStart;
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getOffsetEnd() {
        return this.offsetEnd;
    }

    public void setOffsetEnd(int offsetEnd) {
        this.offsetEnd = offsetEnd;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
