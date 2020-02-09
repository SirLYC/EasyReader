package com.lyc.easyreader.bookshelf.reader.page;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.lyc.easyreader.api.book.BookFile;
import com.lyc.easyreader.bookshelf.reader.page.anim.AnimFactory;
import com.lyc.easyreader.bookshelf.reader.page.anim.PageAnimMode;
import com.lyc.easyreader.bookshelf.reader.page.anim.PageAnimation;

/**
 * Created by Liu Yuchuan on 2020/2/2.
 */
public class PageView extends View {

    private final static String TAG = "BookPageWidget";

    private int viewWidth = 0; // 当前View的宽
    private int viewHeight = 0; // 当前View的高

    private int startX = 0;
    private int startY = 0;
    private boolean isMove = false;
    private PageAnimMode pageAnimMode = PageAnimMode.SIMULATION;
    // 是否允许点击
    private boolean canTouch = true;
    // 唤醒菜单的区域
    private RectF mCenterRect = null;
    private boolean isPrepare;
    // 动画类
    private PageAnimation pageAnim;
    //点击监听
    private TouchListener mTouchListener;
    //内容加载器
    private PageLoader mPageLoader;
    // 动画监听类
    private PageAnimation.OnPageChangeListener mPageAnimListener = new PageAnimation.OnPageChangeListener() {
        @Override
        public boolean hasPrev() {
            return PageView.this.hasPrevPage();
        }

        @Override
        public boolean hasNext() {
            return PageView.this.hasNextPage();
        }

        @Override
        public void pageCancel() {
            PageView.this.pageCancel();
        }
    };

    public PageView(Context context) {
        this(context, null);
    }

    public PageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;

        isPrepare = true;

        if (mPageLoader != null) {
            mPageLoader.prepareDisplay(w, h);
        }
    }

    //设置翻页的模式
    void setPageMode(PageAnimMode pageAnimMode) {
        if (this.pageAnimMode == pageAnimMode && pageAnim != null) {
            return;
        }
        this.pageAnimMode = pageAnimMode;
        if (mPageLoader != null) {
            pageAnim = AnimFactory.INSTANCE.createAnim(pageAnimMode, viewWidth, viewHeight, this, mPageAnimListener);
        }
    }

    public Bitmap getNextBitmap() {
        if (pageAnim == null) return null;
        return pageAnim.getNextBitmap();
    }

    public Bitmap getBgBitmap() {
        if (pageAnim == null) return null;
        return pageAnim.getBgBitmap();
    }

    public boolean autoPrevPage() {
        startPageAnim(PageAnimation.Direction.PRE);
        return true;
    }

    public boolean autoNextPage() {
        startPageAnim(PageAnimation.Direction.NEXT);
        return true;
    }

    private void startPageAnim(PageAnimation.Direction direction) {
        if (mTouchListener == null) return;
        //是否正在执行动画
        abortAnimation();
        if (direction == PageAnimation.Direction.NEXT) {
            int x = viewWidth;
            int y = viewHeight;
            //初始化动画
            pageAnim.setStartPoint(x, y);
            //设置点击点
            pageAnim.setTouchPoint(x, y);
            //设置方向
            boolean hasNext = hasNextPage();

            pageAnim.setDirection(direction);
            if (!hasNext) {
                return;
            }
        } else {
            int x = 0;
            int y = viewHeight;
            //初始化动画
            pageAnim.setStartPoint(x, y);
            //设置点击点
            pageAnim.setTouchPoint(x, y);
            pageAnim.setDirection(direction);
            //设置方向方向
            Boolean hashPrev = hasPrevPage();
            if (!hashPrev) {
                return;
            }
        }
        pageAnim.startAnim();
        this.postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (pageAnim != null && pageAnim.isRunning && pageAnim.getNeedDrawBgColorWhenRunning()) {
            // 初始化参数
            int bgColor = mPageLoader == null ? PageStyle.BG_1.getBgColor() : mPageLoader.getBgColor();
            canvas.drawColor(bgColor);
        }

        //绘制动画
        pageAnim.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (!canTouch && event.getAction() != MotionEvent.ACTION_DOWN) return true;

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                isMove = false;
                canTouch = mTouchListener.onTouch();
                pageAnim.handleTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                // 判断是否大于最小滑动值。
                int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (!isMove) {
                    isMove = Math.abs(startX - event.getX()) > slop || Math.abs(startY - event.getY()) > slop;
                }

                // 如果滑动了，则进行翻页。
                if (isMove) {
                    pageAnim.handleTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isMove) {
                    //设置中间区域范围
                    if (mCenterRect == null) {
                        mCenterRect = new RectF(viewWidth / 5, viewHeight / 3,
                                viewWidth * 4 / 5, viewHeight * 2 / 3);
                    }

                    //是否点击了中间
                    if (mCenterRect.contains(x, y)) {
                        if (mTouchListener != null) {
                            mTouchListener.center();
                        }
                        return true;
                    }
                }
                pageAnim.handleTouchEvent(event);
                break;
        }
        return true;
    }

    /**
     * 判断是否存在上一页
     *
     * @return
     */
    private boolean hasPrevPage() {
        mTouchListener.prePage();
        return mPageLoader.prev();
    }

    /**
     * 判断是否下一页存在
     *
     * @return
     */
    private boolean hasNextPage() {
        mTouchListener.nextPage();
        return mPageLoader.next();
    }

    private void pageCancel() {
        mTouchListener.cancel();
        mPageLoader.pageCancel();
    }

    @Override
    public void computeScroll() {
        //进行滑动
        pageAnim.scrollAnim();
        super.computeScroll();
    }

    //如果滑动状态没有停止就取消状态，重新设置Anim的触碰点
    public void abortAnimation() {
        pageAnim.abortAnim();
    }

    public boolean isRunning() {
        return pageAnimMode != null && pageAnim.isRunning;
    }

    public boolean isPrepare() {
        return isPrepare;
    }

    public void setTouchListener(TouchListener mTouchListener) {
        this.mTouchListener = mTouchListener;
    }

    public void drawNextPage() {
        if (!isPrepare) return;

        pageAnim.changePage();
        mPageLoader.drawPage(getNextBitmap(), false);
    }

    /**
     * 绘制当前页。
     *
     * @param isUpdate
     */
    public void drawCurPage(boolean isUpdate) {
        if (!isPrepare) return;
        mPageLoader.drawPage(getNextBitmap(), isUpdate);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pageAnim.abortAnim();
        pageAnim.clear();

        mPageLoader = null;
        pageAnim = null;
    }

    /**
     * 获取 PageLoader
     *
     * @return
     */
    public PageLoader getPageLoader(BookFile bookFile) {
        // 判是否已经存在
        if (mPageLoader != null) {
            return mPageLoader;
        }
        mPageLoader = new LocalPageLoader(this, bookFile);
        // 判断是否 PageView 已经初始化完成
        if (viewWidth != 0 || viewHeight != 0) {
            // 初始化 PageLoader 的屏幕大小
            mPageLoader.prepareDisplay(viewWidth, viewHeight);
        }

        return mPageLoader;
    }

    public interface TouchListener {
        boolean onTouch();

        void center();

        void prePage();

        void nextPage();

        void cancel();
    }
}
