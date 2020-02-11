package com.lyc.easyreader.api.book;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by Liu Yuchuan on 2020/2/11.
 */
@Entity
public class BookReadRecord {
    @Id
    private String bookId;
    private int chapter;
    private int offsetStart;
    private int offsetEnd;
    private int page;
    private String desc;


    @Generated(hash = 553899228)
    public BookReadRecord() {
    }

    @Generated(hash = 893651149)
    public BookReadRecord(String bookId, int chapter, int offsetStart,
                          int offsetEnd, int page, String desc) {
        this.bookId = bookId;
        this.chapter = chapter;
        this.offsetStart = offsetStart;
        this.offsetEnd = offsetEnd;
        this.page = page;
        this.desc = desc;
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

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getOffsetEnd() {
        return this.offsetEnd;
    }

    public void setOffsetEnd(int offsetEnd) {
        this.offsetEnd = offsetEnd;
    }
}
