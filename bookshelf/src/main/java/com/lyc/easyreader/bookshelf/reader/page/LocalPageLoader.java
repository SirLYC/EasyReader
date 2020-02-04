package com.lyc.easyreader.bookshelf.reader.page;

import com.lyc.easyreader.api.book.BookChapter;
import com.lyc.easyreader.api.book.BookFile;
import com.lyc.easyreader.bookshelf.reader.BookChapterCache;
import com.lyc.easyreader.bookshelf.reader.BookChapterWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;

/**
 * Created by newbiechen on 17-7-1.
 * 问题:
 * 1. 异常处理没有做好
 */

public class LocalPageLoader extends PageLoader {
    private static final String TAG = "LocalPageLoader";
    //默认从文件中获取数据的长度
    private final static int BUFFER_SIZE = 512 * 1024;

    // "序(章)|前言"
    private final static Pattern mPreChapterPattern = Pattern.compile("^(\\s{0,10})((\u5e8f[\u7ae0\u8a00]?)|(\u524d\u8a00)|(\u6954\u5b50))(\\s{0,10})$", Pattern.MULTILINE);

    //正则表达式章节匹配模式
    // "(第)([0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10})([章节回集卷])(.*)"
    private static final String[] CHAPTER_PATTERNS = new String[]{"^(.{0,8})(\u7b2c)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\u7ae0\u8282\u56de\u96c6\u5377])(.{0,30})$",
            "^(\\s{0,4})([\\(\u3010\u300a]?(\u5377)?)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\\.:\uff1a\u0020\f\t])(.{0,30})$",
            "^(\\s{0,4})([\\(\uff08\u3010\u300a])(.{0,30})([\\)\uff09\u3011\u300b])(\\s{0,2})$",
            "^(\\s{0,4})(\u6b63\u6587)(.{0,20})$",
            "^(.{0,4})(Chapter|chapter)(\\s{0,4})([0-9]{1,4})(.{0,30})$"};

    //章节解析模式
    private Pattern mChapterPattern = null;
    //获取书本的文件
    private File mBookFile;
    private BookChapterCache chapterCache = new BookChapterCache();

    public LocalPageLoader(PageView pageView, BookFile collBook) {
        super(pageView, collBook);
        mStatus = STATUS_PARING;
    }

    /**
     * 从文件中提取一章的内容
     *
     * @param chapter
     * @return
     */
    private byte[] getChapterContent(BookChapter chapter) {
        RandomAccessFile bookStream = null;
        try {
            bookStream = new RandomAccessFile(mBookFile, "r");
            bookStream.seek(chapter.getStart());
            int extent = (int) (chapter.getEnd() - chapter.getStart());
            byte[] content = new byte[extent];
            bookStream.read(content, 0, extent);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bookStream != null) {
                try {
                    bookStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new byte[0];
    }

    @Override
    public void saveRecord() {
        super.saveRecord();
        //修改当前COllBook记录
        if (mCollBook != null && isChapterListPrepare) {
//            //表示当前CollBook已经阅读
//            mCollBook.setIsUpdate(false);
//            mCollBook.setLastChapter(mChapterList.get(mCurChapterPos).getTitle());
//            mCollBook.setLastRead(StringUtils.
//                    dateConvert(System.currentTimeMillis(), Constant.FORMAT_BOOK_DATE));
//            //直接更新
//            BookRepository.getInstance()
//                    .saveCollBook(mCollBook);
        }
    }

    @Override
    public void refreshChapterList() {
//        // 对于文件是否存在，或者为空的判断，不作处理。 ==> 在文件打开前处理过了。
//        mBookFile = new File(mCollBook.getCover());
//        //获取文件编码
//        mCharset = FileUtils.getCharset(mBookFile.getAbsolutePath());
//
//        String lastModified = StringUtils.dateConvert(mBookFile.lastModified(), Constant.FORMAT_BOOK_DATE);
//
//        // 判断文件是否已经加载过，并具有缓存
//        if (!mCollBook.isUpdate() && mCollBook.getUpdated() != null
//                && mCollBook.getUpdated().equals(lastModified)
//                && mCollBook.getBookChapters() != null) {
//
//            mChapterList = convertTxtChapter(mCollBook.getBookChapters());
//            isChapterListPrepare = true;
//
//            //提示目录加载完成
//            if (mPageChangeListener != null) {
//                mPageChangeListener.onCategoryFinish(mChapterList);
//            }
//
//            // 加载并显示当前章节
//            openChapter();
//
//            return;
//        }
//
//        // 通过RxJava异步处理分章事件
//        Single.create(new SingleOnSubscribe<Void>() {
//            @Override
//            public void subscribe(SingleEmitter<Void> e) throws Exception {
//                loadChapters();
//                e.onSuccess(new Void());
//            }
//        }).compose(RxUtils::toSimpleSingle)
//                .subscribe(new SingleObserver<Void>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        mChapterDisp = d;
//                    }
//
//                    @Override
//                    public void onSuccess(Void value) {
//                        mChapterDisp = null;
//                        isChapterListPrepare = true;
//
//                        // 提示目录加载完成
//                        if (mPageChangeListener != null) {
//                            mPageChangeListener.onCategoryFinish(mChapterList);
//                        }
//
//                        // 存储章节到数据库
//                        List<BookChapterBean> bookChapterBeanList = new ArrayList<>();
//                        for (int i = 0; i < mChapterList.size(); ++i) {
//                            TxtChapter chapter = mChapterList.get(i);
//                            BookChapterBean bean = new BookChapterBean();
//                            bean.setId(MD5Utils.strToMd5By16(mBookFile.getAbsolutePath()
//                                    + File.separator + chapter.title)); // 将路径+i 作为唯一值
//                            bean.setTitle(chapter.getTitle());
//                            bean.setStart(chapter.getStart());
//                            bean.setUnreadble(false);
//                            bean.setEnd(chapter.getEnd());
//                            bookChapterBeanList.add(bean);
//                        }
//                        mCollBook.setBookChapters(bookChapterBeanList);
//                        mCollBook.setUpdated(lastModified);
//
//                        BookRepository.getInstance().saveBookChaptersWithAsync(bookChapterBeanList);
//                        BookRepository.getInstance().saveCollBook(mCollBook);
//
//                        // 加载并显示当前章节
//                        openChapter();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        chapterError();
//                        LogUtils.d(TAG, "file load error:" + e.toString());
//                    }
//                });
    }

    @Override
    protected BufferedReader getChapterReader(BookChapter chapter) {
        BookChapterWrapper chapterWrapper = chapterCache.get(chapter);
        if (chapterWrapper != null) {
            return chapterWrapper.openBufferedReader();
        } else {
            BookChapterWrapper wrapper = new BookChapterWrapper(chapter, mCollBook);
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
