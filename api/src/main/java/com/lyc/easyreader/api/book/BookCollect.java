package com.lyc.easyreader.api.book;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by Liu Yuchuan on 2020/2/11.
 */
@Entity
public class BookCollect {
    @Id
    private String bookId;
    private boolean collected;

    @Generated(hash = 16879410)
    public BookCollect(String bookId, boolean collected) {
        this.bookId = bookId;
        this.collected = collected;
    }

    @Generated(hash = 1898021084)
    public BookCollect() {
    }

    public String getBookId() {
        return this.bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public boolean getCollected() {
        return this.collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }
}
