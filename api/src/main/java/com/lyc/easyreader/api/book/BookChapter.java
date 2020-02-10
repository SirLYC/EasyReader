package com.lyc.easyreader.api.book;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.converter.PropertyConverter;

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
    @NotNull
    @Convert(converter = ChapterTypeConverter.class, columnType = String.class)
    // 区分是否是通过正则匹配到的
    private ChapterType chapterType;


    @Generated(hash = 1818994638)
    public BookChapter(Long id, int order, long lastModified, @NotNull String bookId, String title,
                       long start, long end, @NotNull ChapterType chapterType) {
        this.id = id;
        this.order = order;
        this.lastModified = lastModified;
        this.bookId = bookId;
        this.title = title;
        this.start = start;
        this.end = end;
        this.chapterType = chapterType;
    }

    @Generated(hash = 1481387400)
    public BookChapter() {
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public ChapterType getChapterType() {
        return this.chapterType;
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

    public void setChapterType(ChapterType chapterType) {
        this.chapterType = chapterType;
    }


    public enum ChapterType {
        REAL, SINGLE, VIRTUAL
    }

    public static class ChapterTypeConverter implements PropertyConverter<ChapterType, String> {

        @Override
        public ChapterType convertToEntityProperty(String databaseValue) {
            return ChapterType.valueOf(databaseValue);
        }

        @Override
        public String convertToDatabaseValue(ChapterType entityProperty) {
            return entityProperty.name();
        }
    }
}
