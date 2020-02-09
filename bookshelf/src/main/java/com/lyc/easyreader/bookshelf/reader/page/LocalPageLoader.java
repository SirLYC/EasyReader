package com.lyc.easyreader.bookshelf.reader.page;

import com.lyc.easyreader.api.book.BookChapter;
import com.lyc.easyreader.api.book.BookFile;
import com.lyc.easyreader.bookshelf.reader.BookChapterCache;
import com.lyc.easyreader.bookshelf.reader.BookChapterWrapper;

import java.io.BufferedReader;

/**
 * Created by Liu Yuchuan on 2020/2/9.
 */

public class LocalPageLoader extends PageLoader {

    private BookChapterCache chapterCache = new BookChapterCache();

    LocalPageLoader(PageView pageView, BookFile collBook) {
        super(pageView, collBook);
        status = STATUS_PARING;
    }

    @Override
    protected BufferedReader getChapterReader(BookChapter chapter) {
        BookChapterWrapper chapterWrapper = chapterCache.get(chapter);
        if (chapterWrapper != null) {
            return chapterWrapper.openBufferedReader();
        } else {
            BookChapterWrapper wrapper = new BookChapterWrapper(chapter, bookFile);
            BufferedReader bufferedReader = wrapper.openBufferedReader();
            if (bufferedReader != null) {
                chapterCache.put(chapter, wrapper);
            }
            return bufferedReader;
        }
    }

    @Override
    protected boolean hasChapterData(BookChapter chapter) {
        return true;
    }
}
