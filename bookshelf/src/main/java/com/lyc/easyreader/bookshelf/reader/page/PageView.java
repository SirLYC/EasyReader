package com.lyc.easyreader.bookshelf.reader.page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;

import com.lyc.easyreader.api.book.BookFile;
import com.lyc.easyreader.bookshelf.reader.page.anim.AnimFactory;
import com.lyc.easyreader.bookshelf.reader.page.anim.PageAnimMode;
import com.lyc.easyreader.bookshelf.reader.page.anim.PageAnimation;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Liu Yuchuan on 2020/2/2.
 */
@SuppressLint("ViewConstructor")
public class PageView extends View implements PageAnimation.OnPageChangeListener {

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
    //内容加载器
    @NonNull
    private final PageLoader pageLoader;
    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMove && pageLoader.enterSelectMode(pendingX, pendingY)) {
                handledLongPressed = true;
            }
        }
    };

    private static final long LONG_PRESS_DELAY = ViewConfiguration.getLongPressTimeout();
    private boolean isSelectModeWhenTouchDown = false;
    private boolean handledLongPressed = false;
    private float pendingX;
    private float pendingY;
    //点击监听
    private PageViewGestureListener pageViewGestureListener;

    public PageView(Context context, BookFile bookFile) {
        super(context);
        pageLoader = new PageLoader(this, bookFile);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw && h != oldh) {
            viewWidth = w;
            viewHeight = h;

            isPrepare = true;

            pageLoader.prepareDisplay(w, h);
        }
    }

    //设置翻页的模式
    void setPageMode(PageAnimMode pageAnimMode) {
        if (this.pageAnimMode == pageAnimMode && pageAnim != null) {
            return;
        }
        this.pageAnimMode = pageAnimMode;
        pageAnim = AnimFactory.INSTANCE.createAnim(pageAnimMode, viewWidth, viewHeight, this, this);
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
        if (pageViewGestureListener == null) return;
        if (pageAnim == null) return;
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
            boolean hasNext = hasNext();

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
            if (!hasPrev()) {
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
            int bgColor = pageLoader.getBgColor();
            canvas.drawColor(bgColor);
        }

        if (pageAnim != null) {
            //绘制动画
            pageAnim.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (pageAnim == null) return false;

        if (!canTouch && event.getAction() != MotionEvent.ACTION_DOWN) return true;

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
                pageLoader.setCanOperateSelectTextAndNotify(pageLoader.isSelectMode());
                if (pageLoader.exitDrag()) {
                    return true;
                }
                if (isMove) {
                    pageAnim.handleTouchEvent(event);
                }
                removeCallbacks(longPressRunnable);
                break;
            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                pendingX = event.getX();
                pendingY = event.getY();
                isMove = false;
                canTouch = pageViewGestureListener == null || pageViewGestureListener.enableTouch();
                pageAnim.handleTouchEvent(event);
                handledLongPressed = false;
                isSelectModeWhenTouchDown = pageLoader.isSelectMode();
                if (isSelectModeWhenTouchDown && pageLoader.hitTestSelectTab(x, y)) {
                    pageLoader.setCanOperateSelectTextAndNotify(false);
                    return true;
                }

                if (!pageLoader.isSelectMode()) {
                    postDelayed(longPressRunnable, LONG_PRESS_DELAY);
                }
                pageLoader.setCanOperateSelectTextAndNotify(false);
                break;
            case MotionEvent.ACTION_MOVE:
                if (pageLoader.isDragTab()) {
                    pageLoader.onDragTab(event.getX(), event.getY());
                    return true;
                }
                if (isSelectModeWhenTouchDown) {
                    return true;
                }
                // 判断是否大于最小滑动值。
                int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (!isMove) {
                    isMove = Math.abs(startX - event.getX()) > slop || Math.abs(startY - event.getY()) > slop;
                }

                // 如果滑动了，则进行翻页。
                if (isMove) {
                    // 移除长按检测
                    removeCallbacks(longPressRunnable);
                    pageAnim.handleTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (pageLoader.exitDrag()) {
                    pageLoader.setCanOperateSelectTextAndNotify(true);
                    return true;
                }

                if (!isMove) {
                    if (handledLongPressed && pageLoader.isSelectMode()) {
                        pageLoader.setCanOperateSelectTextAndNotify(true);
                        return true;
                    }

                    if (pageLoader.isSelectMode()) {
                        pageLoader.setCanOperateSelectTextAndNotify(false);
                    }

                    if (!handledLongPressed && pageLoader.exitSelectMode()) {
                        return true;
                    }

                    removeCallbacks(longPressRunnable);

                    if (pageViewGestureListener != null && pageViewGestureListener.handlePageViewClick()) {
                        return true;
                    }

                    //设置中间区域范围
                    if (mCenterRect == null) {
                        mCenterRect = new RectF(viewWidth / 5f, viewHeight / 5f,
                                viewWidth * 4f / 5f, viewHeight * 4f / 5f);
                    }

                    //是否点击了中间
                    if (mCenterRect.contains(x, y)) {
                        if (pageViewGestureListener != null) {
                            pageViewGestureListener.centerClick();
                        }
                        return true;
                    }
                } else {
                    pageLoader.setCanOperateSelectTextAndNotify(false);
                }

                removeCallbacks(longPressRunnable);
                pageAnim.handleTouchEvent(event);
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (pageAnim != null) {
            //进行滑动
            pageAnim.scrollAnim();
        }
        super.computeScroll();
    }

    //如果滑动状态没有停止就取消状态，重新设置Anim的触碰点
    public void abortAnimation() {
        if (pageAnim != null) {
            pageAnim.abortAnim();
        }
    }

    public boolean isRunning() {
        return pageAnimMode != null && pageAnim != null && pageAnim.isRunning;
    }

    public boolean isPrepare() {
        return isPrepare;
    }

    public void setTouchListener(PageViewGestureListener mPageViewGestureListener) {
        this.pageViewGestureListener = mPageViewGestureListener;
    }

    public void drawNextPage() {
        if (!isPrepare || pageAnim == null) return;

        pageAnim.changePage();
        pageLoader.drawPage(getNextBitmap(), false);
    }

    /**
     * 绘制当前页。
     */
    public void drawCurPage(boolean isUpdate) {
        if (!isPrepare) return;
        pageLoader.drawPage(getNextBitmap(), isUpdate);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(longPressRunnable);
        pageAnim.abortAnim();
        pageAnim.clear();
        pageAnim = null;
    }

    @NotNull
    public PageLoader getPageLoader() {
        return pageLoader;
    }

    @Override
    public boolean hasPrev() {
        pageViewGestureListener.prePage();
        return pageLoader.prev();
    }

    @Override
    public boolean hasNext() {
        pageViewGestureListener.nextPage();
        return pageLoader.next();
    }

    @Override
    public void pageCancel() {
        pageViewGestureListener.cancel();
        pageLoader.pageCancel();
    }

    public interface PageViewGestureListener {

        /**
         * @return false if disable all touch event on this view.
         */
        boolean enableTouch();

        /**
         * @return true if handled
         */
        boolean handlePageViewClick();

        /**
         * Click center
         */
        void centerClick();

        void prePage();

        void nextPage();

        void cancel();
    }
}
