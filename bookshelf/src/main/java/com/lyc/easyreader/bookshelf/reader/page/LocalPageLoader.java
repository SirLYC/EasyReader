package com.lyc.easyreader.bookshelf.reader.page;

import com.lyc.easyreader.api.book.BookChapter;
import com.lyc.easyreader.api.book.BookFile;
import com.lyc.easyreader.bookshelf.reader.BookChapterCache;
import com.lyc.easyreader.bookshelf.reader.BookChapterWrapper;

import java.io.BufferedReader;

/**
 * Created by newbiechen on 17-7-1.
 * 问题:
 * 1. 异常处理没有做好
 */

public class LocalPageLoader extends PageLoader {
    private static final String TAG = "LocalPageLoader";


    private BookChapterCache chapterCache = new BookChapterCache();

    public LocalPageLoader(PageView pageView, BookFile collBook) {
        super(pageView, collBook);
        mStatus = STATUS_PARING;
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
