package com.lyc.easyreader.bookshelf.reader.page;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.lyc.common.thread.ExecutorFactory;
import com.lyc.easyreader.api.book.BookChapter;
import com.lyc.easyreader.api.book.BookFile;
import com.lyc.easyreader.base.ui.theme.NightModeManager;
import com.lyc.easyreader.base.utils.DeviceUtilsKt;
import com.lyc.easyreader.bookshelf.reader.page.anim.PageAnimMode;
import com.lyc.easyreader.bookshelf.utils.StringUtilsKt;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by newbiechen on 17-7-1.
 */

public abstract class PageLoader implements Handler.Callback {
    public static final int TEXT_SIZE_MIN_VALUE_DP = 12;
    public static final int TEXT_SIZE_MAX_VALUE_DP = 32;
    // 当前页面的状态
    public static final int STATUS_LOADING = 1;         // 正在加载
    public static final int STATUS_FINISH = 2;          // 加载完成
    public static final int STATUS_ERROR = 3;           // 加载错误 (一般是网络加载情况)
    public static final int STATUS_EMPTY = 4;           // 空数据
    public static final int STATUS_PARING = 5;          // 正在解析 (装载本地数据)
    public static final int STATUS_PARSE_ERROR = 6;     // 本地文件解析错误(暂未被使用)
    public static final int STATUS_CATEGORY_EMPTY = 7;  // 获取到的目录为空
    private static final int MSG_RELOAD_PAGES = 1;

    private static final String TAG = "PageLoader";

    private static final int TEXT_SIZE_MIN_VALUE = DeviceUtilsKt.dp2px(TEXT_SIZE_MIN_VALUE_DP);
    private static final int TEXT_SIZE_MAX_VALUE = DeviceUtilsKt.dp2px(TEXT_SIZE_MAX_VALUE_DP);

    // 下方小部件
    private static final int WIDGET_MARGIN_BOTTOM = DeviceUtilsKt.dp2px(4);
    private static final int TINY_CHAPTER_MARGIN = DeviceUtilsKt.dp2px(4);

    // 下方小部件的高度
    private float widgetMaxHeight;

    // 当前章节列表
    protected final List<BookChapter> mChapterList;
    // 书本对象
    protected BookFile mCollBook;
    // 监听器
    protected OnPageChangeListener mPageChangeListener;
    /*****************params**************************/
    private float lastTimeTextX;
    // 当前的状态
    protected int mStatus = STATUS_LOADING;
    // 判断章节列表是否加载完成
    protected boolean isChapterListPrepare;
    // 当前章
    protected int mCurChapterPos = 0;
    // 页面显示类
    private PageView mPageView;
    // 当前显示的页
    private BookPage mCurPage;
    // 上一章的页面列表缓存
    private List<BookPage> mPrePageList;
    // 当前章节的页面列表
    private List<BookPage> mCurPageList;
    // 下一章的页面列表缓存
    private List<BookPage> mNextPageList;
    // 绘制电池的画笔
    private Paint mBatteryPaint;
    // 绘制提示的画笔
    private Paint mTipPaint;
    // 绘制标题的画笔
    private Paint mTitlePaint;
    // 存储阅读记录类
//    private BookRecordBean mBookRecord;
    // 绘制背景颜色的画笔(用来擦除需要重绘的部分)
    private Paint mBgPaint;
    // 绘制小说内容的画笔
    private TextPaint mContentTextPaint;
    // 阅读器的配置选项
    // 被遮盖的页，或者认为被取消显示的页
    private BookPage mCancelPage;
    // 是否打开过章节
    private boolean isChapterOpen;
    private boolean isFirstOpen = true;
    private boolean isClose;
    // 页面的翻页效果模式
    private PageAnimMode pageMode = PageAnimMode.SIMULATION;
    // 加载器的颜色主题
    private PageStyle pageStyle = PageStyle.BG_0;
    // 当前是否是夜间模式
    private boolean isNightMode;
    // 书籍绘制区域的宽高
    private int mVisibleWidth;
    private int mVisibleHeight;
    // 应用的宽高
    private int mDisplayWidth;
    private int mDisplayHeight;
    // 间距
    private int marginLeft = DeviceUtilsKt.dp2px(16);
    private int marginRight = DeviceUtilsKt.dp2px(16);
    private int marginTop = DeviceUtilsKt.dp2px(16);
    private int marginBottom = DeviceUtilsKt.dp2px(8);
    // 字体的颜色
    private int mTextColor;
    // 标题的大小
    private int titleTextSize;
    // 字体的大小
    private int contentTextSize;
    // 下方小字大小
    private int tipTextSize;
    // 行间距系数
    private float lineSpaceFactor;
    // 行间距
    private int mTextInterval;
    // 标题的行间距
    private int mTitleInterval;
    // 段间距系数
    private float paraSpaceFactor;
    // 段落距离(基于行间距的额外距离)
    private int mTextPara;
    private int mTitlePara;
    // 电池的百分比
    private int mBatteryLevel;
    // 当前页面的背景
    private int mBgColor;
    // 上一章的记录
    private int mLastChapterPos = 0;
    // 缩进
    private int indentCount;
    private boolean indentFull;
    private String indentString;

    private Handler handler = new Handler(this);

    public PageLoader(PageView pageView, BookFile bookFile) {
        mPageView = pageView;
        mCollBook = bookFile;
        mChapterList = new ArrayList<>(1);

        setIndent(2, true);
        lineSpaceFactor = 0.5f;
        paraSpaceFactor = 1.0f;
        // 初始化数据
        setUpTextParams(DeviceUtilsKt.dp2px(16));
        // 初始化画笔
        initPaint();
        // 初始化PageView
        initPageView();
        // 初始化书籍
        prepareBook();
    }

    /**
     * 作用：设置与文字相关的参数
     *
     * @param textSize
     */
    private void setUpTextParams(int textSize) {
        // 文字大小
        this.contentTextSize = textSize;
        tipTextSize = Math.round(textSize * 0.75f);
        titleTextSize = Math.round(contentTextSize * 1.2f);
        // 行间距(大小为字体的一半)
        mTextInterval = Math.round(contentTextSize * lineSpaceFactor);
        mTitleInterval = Math.round(titleTextSize * lineSpaceFactor);
        // 段落间距(大小为字体的高度)
        mTextPara = Math.round(contentTextSize * paraSpaceFactor);
        mTitlePara = Math.round(titleTextSize * paraSpaceFactor);
    }

    private void initPaint() {
        // 绘制提示的画笔
        mTipPaint = new Paint();
        mTipPaint.setColor(mTextColor);
        mTipPaint.setTextAlign(Paint.Align.LEFT); // 绘制的起始点
        mTipPaint.setTextSize(tipTextSize); // Tip默认的字体大小
        Paint.FontMetrics fontMetrics = mTipPaint.getFontMetrics();
        widgetMaxHeight = fontMetrics.bottom - fontMetrics.top;
        mTipPaint.setAntiAlias(true);
        mTipPaint.setSubpixelText(true);

        // 绘制页面内容的画笔
        mContentTextPaint = new TextPaint();
        mContentTextPaint.setColor(mTextColor);
        mContentTextPaint.setTextSize(contentTextSize);
        mContentTextPaint.setAntiAlias(true);

        // 绘制标题的画笔
        mTitlePaint = new TextPaint();
        mTitlePaint.setColor(mTextColor);
        mTitlePaint.setTextSize(titleTextSize);
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTitlePaint.setAntiAlias(true);

        // 绘制背景的画笔
        mBgPaint = new Paint();
        mBgPaint.setColor(mBgColor);

        // 绘制电池的画笔
        mBatteryPaint = new Paint();
        mBatteryPaint.setAntiAlias(true);
        mBatteryPaint.setDither(true);

        // 初始化页面样式
        setNightMode(NightModeManager.INSTANCE.getNightModeEnable());
    }

    private void initPageView() {
        //配置参数
        mPageView.setPageMode(pageMode);
        mPageView.setBgColor(mBgColor);
    }

    public void setChapterListIfEmpty(List<BookChapter> chapterList) {
        if (mChapterList.isEmpty()) {
            mChapterList.addAll(chapterList);
            if (!mChapterList.isEmpty()) {
                isChapterListPrepare = true;
                openChapter();
            }
        }
    }

    /**
     * 跳转到上一章
     */
    public boolean skipPreChapter() {
        if (!hasPrevChapter()) {
            return false;
        }

        // 载入上一章。
        if (parsePrevChapter()) {
            mCurPage = getCurPage(0);
        } else {
            mCurPage = new BookPage();
        }
        mPageView.drawCurPage(false);
        return true;
    }

    /**
     * 跳转到下一章
     */
    public boolean skipNextChapter() {
        if (!hasNextChapter()) {
            return false;
        }

        //判断是否达到章节的终止点
        if (parseNextChapter()) {
            mCurPage = getCurPage(0);
        } else {
            mCurPage = new BookPage();
        }
        mPageView.drawCurPage(false);
        return true;
    }

    /**
     * 跳转到指定章节
     */
    public void skipToChapter(int pos) {
        // 设置参数
        mCurChapterPos = pos;

        // 将上一章的缓存设置为null
        mPrePageList = null;
        // 将下一章缓存设置为null
        mNextPageList = null;

        // 打开指定章节
        openChapter();
    }

    /**
     * 跳转到指定的页
     */
    public boolean skipToPage(int pos) {
        if (!isChapterListPrepare) {
            return false;
        }
        mCurPage = getCurPage(pos);
        mPageView.drawCurPage(false);
        return true;
    }

    /**
     * 翻到上一页
     */
    public boolean skipToPrePage() {
        return mPageView.autoPrevPage();
    }

    /**
     * 翻到下一页
     */
    public boolean skipToNextPage() {
        return mPageView.autoNextPage();
    }

    /**
     * 更新时间
     */
    public void updateTime() {
        if (!mPageView.isRunning()) {
            mPageView.drawCurPage(true);
        }
    }

    public void updateBattery(int level) {
        mBatteryLevel = level;

        if (!mPageView.isRunning()) {
            mPageView.drawCurPage(true);
        }
    }

    public void setMarginLeft(int marginLeft) {
        setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    public void setMarginRight(int marginRight) {
        setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    public void setMarginTop(int marginTop) {
        setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    public void setMarginBottom(int marginBottom) {
        setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    public void setMargin(int margin) {
        setMargins(margin, margin, margin, margin);
    }

    public void setMargins(int left, int top, int right, int bottom) {
        boolean change = false;
        if (left != marginLeft) {
            marginLeft = left;
            change = true;
        }

        if (top != marginTop) {
            marginTop = top;
            change = true;
        }

        if (right != marginRight) {
            marginRight = right;
            change = true;
        }

        if (bottom != marginBottom) {
            marginBottom = bottom;
            change = true;
        }

        if (!change) {
            return;
        }
        applyVisibleSizeChange();
        postReloadPages();
    }

    public void setIndent(int count, boolean full) {
        if (count != indentCount || full != indentFull) {
            indentCount = count;
            indentFull = full;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append(' ');
            }
            String string = sb.toString();
            indentString = full ? StringUtilsKt.halfToFull(string) : string;
            postReloadPages();
        }
    }

    private void applyVisibleSizeChange() {
        if (mDisplayWidth <= 0 || mDisplayHeight <= 0) {
            return;
        }
        mVisibleWidth = mDisplayWidth - marginLeft - marginRight;
        mVisibleHeight = Math.round(mDisplayHeight - marginTop - marginBottom - WIDGET_MARGIN_BOTTOM - widgetMaxHeight);
    }

    /**
     * 设置文字相关参数
     */
    public void setContentTextSize(int textSize) {
        if (textSize < TEXT_SIZE_MIN_VALUE) {
            textSize = TEXT_SIZE_MIN_VALUE;
        } else if (textSize > TEXT_SIZE_MAX_VALUE) {
            textSize = TEXT_SIZE_MAX_VALUE;
        }
        if (textSize != contentTextSize) {
            // 设置文字相关参数
            setUpTextParams(textSize);
            mTipPaint.setTextSize(tipTextSize);
            mContentTextPaint.setTextSize(contentTextSize);
            mTitlePaint.setTextSize(titleTextSize);
            postReloadPages();
        }
    }

    public float getLineSpaceFactor() {
        return lineSpaceFactor;
    }

    public void setLineSpaceFactor(float lineSpaceFactor) {
        if (lineSpaceFactor != this.lineSpaceFactor) {
            this.lineSpaceFactor = lineSpaceFactor;
            setUpTextParams(contentTextSize);
            postReloadPages();
        }
    }

    public float getParaSpaceFactor() {
        return paraSpaceFactor;
    }

    public void setParaSpaceFactor(float paraSpaceFactor) {
        if (paraSpaceFactor != this.paraSpaceFactor) {
            this.paraSpaceFactor = paraSpaceFactor;
            setUpTextParams(contentTextSize);
            postReloadPages();
        }
    }

    private void postReloadPages() {
        handler.removeMessages(MSG_RELOAD_PAGES);
        handler.sendEmptyMessage(MSG_RELOAD_PAGES);
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (msg.what == MSG_RELOAD_PAGES) {
            reloadPagesIfNeeded();
            return true;
        }

        return false;
    }

    private void reloadPagesIfNeeded() {
        if (mDisplayWidth == 0 || mDisplayHeight == 0) {
            return;
        }
        // 取消缓存
        mPrePageList = null;
        mNextPageList = null;

        // 如果当前已经显示数据
        if (isChapterListPrepare && mStatus == STATUS_FINISH) {
            // 重新计算当前页面
            dealLoadPageList(mCurChapterPos);

            // 防止在最后一页，通过修改字体，以至于页面数减少导致崩溃的问题
            if (mCurPage.position >= mCurPageList.size()) {
                mCurPage.position = mCurPageList.size() - 1;
            }

            // 重新获取指定页面
            mCurPage = mCurPageList.get(mCurPage.position);
            mPageView.drawCurPage(false);
        }
    }

    /**
     * 设置夜间模式
     *
     * @param nightMode
     */
    public void setNightMode(boolean nightMode) {
        isNightMode = nightMode;

        if (isNightMode) {
            mBatteryPaint.setColor(Color.WHITE);
            setPageStyle(PageStyle.NIGHT);
        } else {
            mBatteryPaint.setColor(Color.BLACK);
            setPageStyle(pageStyle);
        }
    }

    /**
     * 设置页面样式
     *
     * @param pageStyle:页面样式
     */
    public void setPageStyle(PageStyle pageStyle) {
        if (pageStyle != PageStyle.NIGHT) {
            this.pageStyle = pageStyle;
//            mSettingManager.setPageStyle(pageStyle);
        }

        if (isNightMode && pageStyle != PageStyle.NIGHT) {
            return;
        }

        // 设置当前颜色样式
        mTextColor = pageStyle.getFontColor();
        mBgColor = pageStyle.getBgColor();

        mTipPaint.setColor(mTextColor);
        mTitlePaint.setColor(mTextColor);
        mContentTextPaint.setColor(mTextColor);

        mBgPaint.setColor(mBgColor);

        mPageView.drawCurPage(false);
    }

    /**
     * 翻页动画
     *
     * @param pageAnimMode:翻页模式
     * @see PageAnimMode
     */
    public void setPageAnimMode(PageAnimMode pageAnimMode) {
        pageMode = pageAnimMode;

        mPageView.setPageMode(pageMode);

        // 重新绘制当前页
        mPageView.drawCurPage(false);
    }

    /**
     * 设置页面切换监听
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mPageChangeListener = listener;

        // 如果目录加载完之后才设置监听器，那么会默认回调
        if (isChapterListPrepare) {
            mPageChangeListener.onCategoryFinish(mChapterList);
        }
    }

    /**
     * 获取当前页的状态
     *
     * @return
     */
    public int getPageStatus() {
        return mStatus;
    }

    /**
     * 获取书籍信息
     *
     * @return
     */
    public BookFile getCollBook() {
        return mCollBook;
    }

    /**
     * 获取章节目录。
     *
     * @return
     */
    public List<BookChapter> getChapterCategory() {
        return mChapterList;
    }

    /**
     * 获取当前页的页码
     *
     * @return
     */
    public int getPagePos() {
        return mCurPage.position;
    }

    /**
     * 获取当前章节的章节位置
     *
     * @return
     */
    public int getChapterPos() {
        return mCurChapterPos;
    }

    /**
     * 保存阅读记录
     */
    public void saveRecord() {

        if (mChapterList.isEmpty()) {
            return;
        }
    }

    /**
     * 初始化书籍
     */
    private void prepareBook() {
//        mBookRecord = BookRepository.getInstance()
//                .getBookRecord(mCollBook.get_id());
//
//        if (mBookRecord == null) {
//            mBookRecord = new BookRecordBean();
//        }
//
//        mCurChapterPos = mBookRecord.getChapter();
        mLastChapterPos = mCurChapterPos;
    }

    /**
     * 打开指定章节
     */
    public void openChapter() {
        isFirstOpen = false;

        if (!mPageView.isPrepare()) {
            return;
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            mStatus = STATUS_LOADING;
            mPageView.drawCurPage(false);
            return;
        }

        // 如果获取到的章节目录为空
        if (mChapterList.isEmpty()) {
            mStatus = STATUS_CATEGORY_EMPTY;
            mPageView.drawCurPage(false);
            return;
        }

        if (parseCurChapter()) {
            // 如果章节从未打开
            if (!isChapterOpen) {
//                int position = mBookRecord.getPagePos();
                int position = 0;
                // 防止记录页的页号，大于当前最大页号
                if (position >= mCurPageList.size()) {
                    position = mCurPageList.size() - 1;
                }
                mCurPage = getCurPage(position);
                mCancelPage = mCurPage;
                // 切换状态
                isChapterOpen = true;
            } else {
                mCurPage = getCurPage(0);
            }
        } else {
            mCurPage = new BookPage();
        }

        mPageView.drawCurPage(false);
    }

    public void chapterError() {
        //加载错误
        mStatus = STATUS_ERROR;
        mPageView.drawCurPage(false);
    }

    /**
     * 关闭书本
     */
    public void closeBook() {
        isChapterListPrepare = false;
        isClose = true;

        clearList(mChapterList);
        clearList(mCurPageList);
        clearList(mNextPageList);

        mChapterList.clear();
        mCurPageList = null;
        mNextPageList = null;
        mPageView = null;
        mCurPage = null;
    }

    private void clearList(List list) {
        if (list != null) {
            list.clear();
        }
    }

    public boolean isClose() {
        return isClose;
    }

    public boolean isChapterOpen() {
        return isChapterOpen;
    }

    /**
     * 加载页面列表
     *
     * @param chapterPos:章节序号
     */
    private List<BookPage> loadPageList(int chapterPos) throws Exception {
        // 获取章节
        BookChapter chapter = mChapterList.get(chapterPos);
        // 判断章节是否存在
        if (!hasChapterData(chapter)) {
            return null;
        }
        // 获取章节的文本流
        BufferedReader reader = getChapterReader(chapter);
        if (reader == null) {
            return null;
        }
        return loadPages(chapter, reader);
    }

    /*******************************abstract method***************************************/

    /**
     * 刷新章节列表
     */
    public abstract void refreshChapterList();

    /**
     * 获取章节的文本流
     *
     * @param chapter
     * @return
     */
    protected abstract BufferedReader getChapterReader(BookChapter chapter) throws Exception;

    /**
     * 章节数据是否存在
     *
     * @return
     */
    protected abstract boolean hasChapterData(BookChapter chapter);

    /***********************************default method***********************************************/

    void drawPage(Bitmap bitmap, boolean isUpdate) {
        drawBackground(mPageView.getBgBitmap(), isUpdate);
        if (!isUpdate) {
            drawContent(bitmap);
        }
        //更新绘制
        mPageView.invalidate();
    }

    private void drawBackground(Bitmap bitmap, boolean isUpdate) {
        Canvas canvas = new Canvas(bitmap);

        if (!isUpdate) {
            // 绘制背景
            canvas.drawColor(mBgColor);
        } else if (lastTimeTextX > 0) {
            // 擦除右下角的区域
            // 准备重绘
            mBgPaint.setColor(mBgColor);
            canvas.drawRect(lastTimeTextX, mDisplayHeight - WIDGET_MARGIN_BOTTOM - widgetMaxHeight, mDisplayWidth, mDisplayHeight - WIDGET_MARGIN_BOTTOM, mBgPaint);
        }

        // 绘制电池
        int visibleRight = mDisplayWidth - marginRight;
        int visibleBottom = mDisplayHeight - WIDGET_MARGIN_BOTTOM;

        int outFrameWidth = (int) mTipPaint.measureText("xxx");
        int outFrameHeight = (int) mTipPaint.getTextSize();

        int polarHeight = DeviceUtilsKt.dp2px(6);
        int polarWidth = DeviceUtilsKt.dp2px(2);
        int border = 1;
        int innerMargin = 1;

        //电极的制作
        int polarLeft = visibleRight - polarWidth;
        int polarTop = visibleBottom - (outFrameHeight + polarHeight) / 2;
        Rect polar = new Rect(polarLeft, polarTop, visibleRight,
                polarTop + polarHeight - DeviceUtilsKt.dp2px(2));

        mBatteryPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(polar, mBatteryPaint);

        //外框的制作
        int outFrameLeft = polarLeft - outFrameWidth;
        int outFrameTop = visibleBottom - outFrameHeight;
        int outFrameBottom = visibleBottom - DeviceUtilsKt.dp2px(2);
        Rect outFrame = new Rect(outFrameLeft, outFrameTop, polarLeft, outFrameBottom);

        mBatteryPaint.setStyle(Paint.Style.STROKE);
        mBatteryPaint.setStrokeWidth(border);
        canvas.drawRect(outFrame, mBatteryPaint);

        //内框的制作
        float innerWidth = (outFrame.width() - innerMargin * 2 - border) * (mBatteryLevel / 100.0f);
        RectF innerFrame = new RectF(outFrameLeft + border + innerMargin, outFrameTop + border + innerMargin,
                outFrameLeft + border + innerMargin + innerWidth, outFrameBottom - border - innerMargin);

        mBatteryPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(innerFrame, mBatteryPaint);

        // 绘制当前时间
        //底部的字显示的位置Y
        float y = mDisplayHeight - mTipPaint.getFontMetrics().bottom - WIDGET_MARGIN_BOTTOM;
        String time = StringUtilsKt.formatReaderTime(System.currentTimeMillis());
        float x = outFrameLeft - mTipPaint.measureText(time) - DeviceUtilsKt.dp2px(4);
        lastTimeTextX = x;
        canvas.drawText(time, x, y, mTipPaint);
        float rightDis = mDisplayWidth - (x - TINY_CHAPTER_MARGIN);

        if (!isUpdate) {

            if (!mChapterList.isEmpty()) {
                // 绘制页码
                // 底部的字显示的位置Y
                y = mDisplayHeight - mTipPaint.getFontMetrics().bottom - WIDGET_MARGIN_BOTTOM;
                float leftDis = 0f;
                // 只有finish的时候采用页码
                if (mStatus == STATUS_FINISH) {
                    String percent = (mCurPage.position + 1) + "/" + mCurPageList.size();
                    canvas.drawText(percent, marginLeft, y, mTipPaint);
                    leftDis = mTipPaint.measureText(percent) + TINY_CHAPTER_MARGIN + marginLeft;
                }

                String pendingTitle = null;
                if (mStatus != STATUS_FINISH) {
                    if (isChapterListPrepare) {
                        pendingTitle = mChapterList.get(mCurChapterPos).getTitle();
                    }
                } else {
                    pendingTitle = mCurPage.title;
                }

                if (pendingTitle != null) {
                    float maxWidth = mDisplayWidth - Math.max(leftDis, rightDis) * 2;
                    if (maxWidth > 0) {
                        int count = mTipPaint.breakText(pendingTitle, true, maxWidth, null);
                        if (count > 0) {
                            Paint.Align orgAlign = mTipPaint.getTextAlign();
                            mTipPaint.setTextAlign(Paint.Align.CENTER);
                            if (count != pendingTitle.length()) {
                                canvas.drawText(pendingTitle.substring(0, count), mDisplayWidth * 0.5f, y, mTipPaint);
                            } else {
                                canvas.drawText(pendingTitle, mDisplayWidth * 0.5f, y, mTipPaint);
                            }
                            mTipPaint.setTextAlign(orgAlign);
                        }
                    }
                }
            }
        }
    }

    private void drawContent(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);

        if (mStatus != STATUS_FINISH) {
            //绘制字体
            String tip = "";
            switch (mStatus) {
                case STATUS_LOADING:
                    tip = "正在拼命加载中...";
                    break;
                case STATUS_ERROR:
                    tip = "加载失败(点击边缘重试)";
                    break;
                case STATUS_EMPTY:
                    tip = "文章内容为空";
                    break;
                case STATUS_PARING:
                    tip = "正在排版请等待...";
                    break;
                case STATUS_PARSE_ERROR:
                    tip = "文件解析错误";
                    break;
                case STATUS_CATEGORY_EMPTY:
                    tip = "目录列表为空";
                    break;
            }

            //将提示语句放到正中间
            Paint.FontMetrics fontMetrics = mContentTextPaint.getFontMetrics();
            float textHeight = fontMetrics.top - fontMetrics.bottom;
            float textWidth = mContentTextPaint.measureText(tip);
            float pivotX = (mDisplayWidth - textWidth) / 2;
            float pivotY = (mDisplayHeight - textHeight) / 2;
            canvas.drawText(tip, pivotX, pivotY, mContentTextPaint);
        } else {
            float top = marginTop - mContentTextPaint.getFontMetrics().top;

            //设置总距离
            int interval = mTextInterval + (int) mContentTextPaint.getTextSize();
            int para = mTextPara + (int) mContentTextPaint.getTextSize();
            int titleInterval = mTitleInterval + (int) mTitlePaint.getTextSize();
            int titlePara = mTitlePara + (int) mContentTextPaint.getTextSize();
            String str;

            //对标题进行绘制
            for (int i = 0; i < mCurPage.titleLines; ++i) {
                str = mCurPage.lines.get(i);

                //设置顶部间距
                if (i == 0) {
                    top += mTitlePara;
                }

                //计算文字显示的起始点
                int start = (int) (mDisplayWidth - mTitlePaint.measureText(str)) / 2;
                //进行绘制
                canvas.drawText(str, start, top, mTitlePaint);

                //设置尾部间距
                if (i == mCurPage.titleLines - 1) {
                    top += titlePara;
                } else {
                    //行间距
                    top += titleInterval;
                }
            }

            //对内容进行绘制
            for (int i = mCurPage.titleLines; i < mCurPage.lines.size(); ++i) {
                str = mCurPage.lines.get(i);

                canvas.drawText(str, marginLeft, top, mContentTextPaint);
                if (str.endsWith("\n")) {
                    top += para;
                } else {
                    top += interval;
                }
            }
        }
    }

    void prepareDisplay(int w, int h) {
        // 获取PageView的宽高
        mDisplayWidth = w;
        mDisplayHeight = h;

        // 获取内容显示位置的大小
        applyVisibleSizeChange();

        // 重置 PageMode
        mPageView.setPageMode(pageMode);

        if (!isChapterOpen) {
            // 展示加载界面
            mPageView.drawCurPage(false);
            // 如果在 display 之前调用过 openChapter 肯定是无法打开的。
            // 所以需要通过 display 再重新调用一次。
            if (!isFirstOpen) {
                // 打开书籍
                openChapter();
            }
        } else {
            // 如果章节已显示，那么就重新计算页面
            if (mStatus == STATUS_FINISH) {
                dealLoadPageList(mCurChapterPos);
                // 重新设置文章指针的位置
                mCurPage = getCurPage(mCurPage.position);
            }
            mPageView.drawCurPage(false);
        }
    }

    /**
     * 翻阅上一页
     */
    boolean prev() {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false;
        }

        if (mStatus == STATUS_FINISH) {
            // 先查看是否存在上一页
            BookPage prevPage = getPrevPage();
            if (prevPage != null) {
                mCancelPage = mCurPage;
                mCurPage = prevPage;
                mPageView.drawNextPage();
                return true;
            }
        }

        if (!hasPrevChapter()) {
            return false;
        }

        mCancelPage = mCurPage;
        if (parsePrevChapter()) {
            mCurPage = getPrevLastPage();
        } else {
            mCurPage = new BookPage();
        }
        mPageView.drawNextPage();
        return true;
    }

    /**
     * 解析上一章数据
     *
     * @return 数据是否解析成功
     */
    private boolean parsePrevChapter() {
        // 加载上一章数据
        int prevChapter = mCurChapterPos - 1;

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = prevChapter;

        // 当前章缓存为下一章
        mNextPageList = mCurPageList;

        // 判断是否具有上一章缓存
        if (mPrePageList != null) {
            mCurPageList = mPrePageList;
            mPrePageList = null;

            // 回调
            chapterChangeCallback();
        } else {
            dealLoadPageList(prevChapter);
        }
        return mCurPageList != null;
    }

    private boolean hasPrevChapter() {
        //判断是否上一章节为空
        return mCurChapterPos - 1 >= 0;
    }

    boolean next() {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false;
        }

        if (mStatus == STATUS_FINISH) {
            // 先查看是否存在下一页
            BookPage nextPage = getNextPage();
            if (nextPage != null) {
                mCancelPage = mCurPage;
                mCurPage = nextPage;
                mPageView.drawNextPage();
                return true;
            }
        }

        if (!hasNextChapter()) {
            return false;
        }

        mCancelPage = mCurPage;
        // 解析下一章数据
        if (parseNextChapter()) {
            mCurPage = mCurPageList.get(0);
        } else {
            mCurPage = new BookPage();
        }
        mPageView.drawNextPage();
        return true;
    }

    private boolean hasNextChapter() {
        // 判断是否到达目录最后一章
        return mCurChapterPos + 1 < mChapterList.size();
    }

    private boolean parseCurChapter() {
        // 解析数据
        dealLoadPageList(mCurChapterPos);
        // 预加载下一页面
        preLoadNextChapter();
        return mCurPageList != null;
    }

    /**
     * 解析下一章数据
     */
    private boolean parseNextChapter() {
        int nextChapter = mCurChapterPos + 1;

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = nextChapter;

        // 将当前章的页面列表，作为上一章缓存
        mPrePageList = mCurPageList;

        // 是否下一章数据已经预加载了
        if (mNextPageList != null) {
            mCurPageList = mNextPageList;
            mNextPageList = null;
            // 回调
            chapterChangeCallback();
        } else {
            // 处理页面解析
            dealLoadPageList(nextChapter);
        }
        // 预加载下一页面
        preLoadNextChapter();
        return mCurPageList != null;
    }

    private void dealLoadPageList(int chapterPos) {
        try {
            mCurPageList = loadPageList(chapterPos);
            if (mCurPageList != null) {
                if (mCurPageList.isEmpty()) {
                    mStatus = STATUS_EMPTY;

                    // 添加一个空数据
                    BookPage page = new BookPage();
                    page.lines = new ArrayList<>(1);
                    mCurPageList.add(page);
                } else {
                    mStatus = STATUS_FINISH;
                }
            } else {
                mStatus = STATUS_LOADING;
            }
        } catch (Exception e) {
            e.printStackTrace();

            mCurPageList = null;
            mStatus = STATUS_ERROR;
        }

        // 回调
        chapterChangeCallback();
    }

    private void chapterChangeCallback() {
        if (mPageChangeListener != null) {
            mPageChangeListener.onChapterChange(mCurChapterPos);
            mPageChangeListener.onPageCountChange(mCurPageList != null ? mCurPageList.size() : 0);
        }
    }

    // 预加载下一章
    private void preLoadNextChapter() {
        final int nextChapter = mCurChapterPos + 1;

        // 如果不存在下一章，且下一章没有数据，则不进行加载。
        if (!hasNextChapter() || !hasChapterData(mChapterList.get(nextChapter))) {
            return;
        }

        ExecutorFactory.INSTANCE.getIO_EXECUTOR().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<BookPage> list = loadPageList(nextChapter);
                    ExecutorFactory.INSTANCE.getMAIN_EXECUTOR().execute(new Runnable() {
                        @Override
                        public void run() {
                            mNextPageList = list;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 取消翻页
    void pageCancel() {
        if (mCurPage.position == 0 && mCurChapterPos > mLastChapterPos) { // 加载到下一章取消了
            if (mPrePageList != null) {
                cancelNextChapter();
            } else {
                if (parsePrevChapter()) {
                    mCurPage = getPrevLastPage();
                } else {
                    mCurPage = new BookPage();
                }
            }
        } else if (mCurPageList == null
                || (mCurPage.position == mCurPageList.size() - 1
                && mCurChapterPos < mLastChapterPos)) {  // 加载上一章取消了

            if (mNextPageList != null) {
                cancelPreChapter();
            } else {
                if (parseNextChapter()) {
                    mCurPage = mCurPageList.get(0);
                } else {
                    mCurPage = new BookPage();
                }
            }
        } else {
            // 假设加载到下一页，又取消了。那么需要重新装载。
            mCurPage = mCancelPage;
        }
    }

    private void cancelNextChapter() {
        int temp = mLastChapterPos;
        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = temp;

        mNextPageList = mCurPageList;
        mCurPageList = mPrePageList;
        mPrePageList = null;

        chapterChangeCallback();

        mCurPage = getPrevLastPage();
        mCancelPage = null;
    }

    private void cancelPreChapter() {
        // 重置位置点
        int temp = mLastChapterPos;
        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = temp;
        // 重置页面列表
        mPrePageList = mCurPageList;
        mCurPageList = mNextPageList;
        mNextPageList = null;

        chapterChangeCallback();

        mCurPage = getCurPage(0);
        mCancelPage = null;
    }

    /**************************************private method********************************************/
    /**
     * 将章节数据，解析成页面列表
     *
     * @param chapter：章节信息
     * @param br：章节的文本流
     */
    private List<BookPage> loadPages(BookChapter chapter, BufferedReader br) {
        //生成的页面
        List<BookPage> pages = new ArrayList<>();
        //使用流的方式加载
        List<String> lines = new ArrayList<>();
        int rHeight = mVisibleHeight;
        int titleLinesCount = 0;
        boolean showTitle = true; // 是否展示标题
        String paragraph = chapter.getTitle();//默认展示标题
        try {
            while (showTitle || (paragraph = br.readLine()) != null) {
                // 重置段落
                if (!showTitle) {
                    paragraph = paragraph.replaceAll("\\s", "");
                    // 如果只有换行符，那么就不执行
                    if (TextUtils.isEmpty(paragraph)) continue;
                    paragraph = indentString == null ? "" : indentString + paragraph + "\n";
                } else {
                    //设置 title 的顶部间距
                    rHeight -= mTitlePara;
                }
                int wordCount;
                String subStr;
                while (paragraph.length() > 0) {
                    //当前空间，是否容得下一行文字
                    if (showTitle) {
                        rHeight -= mTitlePaint.getTextSize();
                    } else {
                        rHeight -= mContentTextPaint.getTextSize();
                    }
                    // 一页已经填充满了，创建 TextPage
                    if (rHeight <= 0) {
                        // 创建Page
                        BookPage page = new BookPage();
                        page.position = pages.size();
                        page.title = chapter.getTitle();
                        page.lines = new ArrayList<>(lines);
                        page.titleLines = titleLinesCount;
                        pages.add(page);
                        // 重置Lines
                        lines.clear();
                        rHeight = mVisibleHeight;
                        titleLinesCount = 0;

                        continue;
                    }

                    //测量一行占用的字节数
                    if (showTitle) {
                        wordCount = mTitlePaint.breakText(paragraph,
                                true, mVisibleWidth, null);
                    } else {
                        wordCount = mContentTextPaint.breakText(paragraph,
                                true, mVisibleWidth, null);
                    }

                    subStr = paragraph.substring(0, wordCount);
                    if (!subStr.equals("\n")) {
                        //将一行字节，存储到lines中
                        lines.add(subStr);

                        //设置段落间距
                        if (showTitle) {
                            titleLinesCount += 1;
                            rHeight -= mTitleInterval;
                        } else {
                            rHeight -= mTextInterval;
                        }
                    }
                    //裁剪
                    paragraph = paragraph.substring(wordCount);
                }

                //增加段落的间距
                if (!showTitle && lines.size() != 0) {
                    rHeight = rHeight - mTextPara + mTextInterval;
                }

                if (showTitle) {
                    rHeight = rHeight - mTitlePara + mTitleInterval;
                    showTitle = false;
                }
            }

            if (lines.size() != 0) {
                //创建Page
                BookPage page = new BookPage();
                page.position = pages.size();
                page.title = chapter.getTitle();
                page.lines = new ArrayList<>(lines);
                page.titleLines = titleLinesCount;
                pages.add(page);
                //重置Lines
                lines.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return pages;
    }


    private BookPage getCurPage(int pos) {
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(pos);
        }
        return mCurPageList.get(pos);
    }

    private BookPage getPrevPage() {
        int pos = mCurPage.position - 1;
        if (pos < 0) {
            return null;
        }
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(pos);
        }
        return mCurPageList.get(pos);
    }

    private BookPage getNextPage() {
        int pos = mCurPage.position + 1;
        if (pos >= mCurPageList.size()) {
            return null;
        }
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(pos);
        }
        return mCurPageList.get(pos);
    }

    private BookPage getPrevLastPage() {
        int pos = mCurPageList.size() - 1;

        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(pos);
        }

        return mCurPageList.get(pos);
    }

    private boolean canTurnPage() {

        if (!isChapterListPrepare) {
            return false;
        }

        if (mStatus == STATUS_PARSE_ERROR
                || mStatus == STATUS_PARING) {
            return false;
        } else if (mStatus == STATUS_ERROR) {
            mStatus = STATUS_LOADING;
        }
        return true;
    }

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
    }

    /*****************************************interface*****************************************/

    public interface OnPageChangeListener {
        /**
         * 作用：章节切换的时候进行回调
         *
         * @param pos:切换章节的序号
         */
        void onChapterChange(int pos);

        /**
         * 作用：请求加载章节内容
         *
         * @param requestChapters:需要下载的章节列表
         */
        void requestChapters(List<BookChapter> requestChapters);

        /**
         * 作用：章节目录加载完成时候回调
         *
         * @param chapters：返回章节目录
         */
        void onCategoryFinish(List<BookChapter> chapters);

        /**
         * 作用：章节页码数量改变之后的回调。==> 字体大小的调整，或者是否关闭虚拟按钮功能都会改变页面的数量。
         *
         * @param count:页面的数量
         */
        void onPageCountChange(int count);

        /**
         * 作用：当页面改变的时候回调
         *
         * @param pos:当前的页面的序号
         */
        void onPageChange(int pos);
    }
}
