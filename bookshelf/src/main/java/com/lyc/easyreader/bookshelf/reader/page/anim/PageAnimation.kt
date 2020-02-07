package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.utils.LogUtils
import kotlin.math.abs

/**
 * Created by Liu Yuchuan on 2020/2/5.
 */
abstract class PageAnimation internal constructor(
    //屏幕的尺寸
    @JvmField
    protected val screenWidth: Int,
    @JvmField
    protected val screenHeight: Int,
    @JvmField
    //正在使用的View
    protected var view: View?,
    protected val listener: OnPageChangeListener
) {
    //滑动装置
    @JvmField
    protected val scroller = Scroller(ReaderApplication.appContext(), LinearInterpolator())
    //移动方向
    open var direction = Direction.NONE
    @JvmField
    var isRunning = false
    //视图的尺寸
    @JvmField
    protected val viewWidth: Int = screenWidth
    @JvmField
    protected val viewHeight: Int = screenHeight
    //起始点
    @JvmField
    protected var startX = 0f
    @JvmField
    protected var startY = 0f
    //触碰点
    @JvmField
    protected var touchX = 0f
    @JvmField
    protected var touchY = 0f
    //上一个触碰点
    @JvmField
    protected var mLastX = 0f
    @JvmField
    protected var mLastY = 0f

    @JvmField
    protected var curBitmap: Bitmap =
        Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565)
    var nextBitmap: Bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565)
        protected set
    val bgBitmap: Bitmap
        get() = nextBitmap

    private val slop = ViewConfiguration.get(ReaderApplication.appContext()).scaledTouchSlop


    //是否取消翻页
    @JvmField
    protected var isCancel = false
    //可以使用 mLast代替
    private var mMoveX = 0
    private var mMoveY = 0
    //是否移动了
    private var isMove = false
    //是否翻阅下一页。true表示翻到下一页，false表示上一页。
    private var isNext = false
    //是否没下一页或者上一页
    private var noNext = false

    /**
     * 转换页面，在显示下一章的时候，必须首先调用此方法
     */
    fun changePage() {
        val bitmap = curBitmap
        curBitmap = nextBitmap
        nextBitmap = bitmap
    }

    open fun setStartPoint(x: Float, y: Float) {
        startX = x
        startY = y
        mLastX = startX
        mLastY = startY
    }

    open fun setTouchPoint(x: Float, y: Float) {
        mLastX = touchX
        mLastY = touchY
        touchX = x
        touchY = y
    }

    /**
     * 开启翻页动画
     */
    fun startAnim() {
        if (isRunning) {
            return
        }
        isRunning = true
        startAnimImp()
    }

    open fun startAnimImp() {
        val dx: Int
        if (direction === Direction.NEXT) {
            if (isCancel) {
                var dis = (screenWidth - startX + touchX).toInt()
                if (dis > screenWidth) {
                    dis = screenWidth
                }
                dx = screenWidth - dis
            } else {
                dx = (-(touchX + (screenWidth - startX))).toInt()
            }
        } else {
            dx = if (isCancel) {
                (-abs(touchX - startX)).toInt()
            } else {
                (screenWidth - (touchX - startX)).toInt()
            }
        }
        //滑动速度保持一致
        val duration = 400 * abs(dx) / screenWidth
        scroller.startScroll(touchX.toInt(), 0, dx, 0, duration)
    }

    fun clear() {
        view = null
    }

    /**
     * 点击事件的处理
     */
    fun handleTouchEvent(event: MotionEvent) {
        //获取点击位置
        val x = event.x.toInt()
        val y = event.y.toInt()
        //设置触摸点
        setTouchPoint(x.toFloat(), y.toFloat())
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //移动的点击位置
                mMoveX = 0
                mMoveY = 0
                //是否移动
                isMove = false
                //是否存在下一章
                noNext = false
                //是下一章还是前一章
                isNext = false
                //是否正在执行动画
                isRunning = false
                //取消
                isCancel = false
                //设置起始位置的触摸点
                setStartPoint(x.toFloat(), y.toFloat())
                //如果存在动画则取消动画
                abortAnim()
            }
            MotionEvent.ACTION_MOVE -> {

                //判断是否移动了
                if (!isMove) {
                    isMove = abs(startX - x) > slop || abs(startY - y) > slop
                }
                if (isMove) { //判断是否是准备移动的状态(将要移动但是还没有移动)
                    if (mMoveX == 0 && mMoveY == 0) { //判断翻得是上一页还是下一页
                        if (x - startX > 0) { //上一页的参数配置
                            isNext = false
                            val hasPrev = listener.hasPrev()
                            direction =
                                Direction.PRE
                            //如果上一页不存在
                            if (!hasPrev) {
                                noNext = true
                                return
                            }
                        } else { //进行下一页的配置
                            isNext = true
                            //判断是否下一页存在
                            val hasNext = listener.hasNext()
                            //如果存在设置动画方向
                            direction =
                                Direction.NEXT
                            //如果不存在表示没有下一页了
                            if (!hasNext) {
                                noNext = true
                                return
                            }
                        }
                    } else {
                        //判断是否取消翻页
                        LogUtils.d("AAA", "x=$x, moveX=${mMoveX}")
                        val dx = x - mMoveX
                        // 当滑动到一定距离时，才可判定是否需要取消翻页
                        if (abs(dx) > slop) {
                            isCancel = if (isNext) {
                                dx > 0
                            } else {
                                dx < 0
                            }
                        }
                    }
                    mMoveX = x
                    mMoveY = y
                    isRunning = true
                    view?.invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isMove) {
                    isNext = x >= screenWidth / 2
                    if (isNext) { //判断是否下一页存在
                        val hasNext = listener.hasNext()
                        //设置动画方向
                        direction =
                            Direction.NEXT
                        if (!hasNext) {
                            return
                        }
                    } else {
                        val hasPrev = listener.hasPrev()
                        direction =
                            Direction.PRE
                        if (!hasPrev) {
                            return
                        }
                    }
                }
                // 是否取消翻页
                if (isCancel) {
                    listener.pageCancel()
                }
                // 开启翻页效果
                if (!noNext) {
                    isRunning = false
                    startAnim()
                    view?.invalidate()
                }
            }
        }
    }

    /**
     * 绘制图形
     *
     * @param canvas
     */
    fun draw(canvas: Canvas) {
        if (isRunning) {
            drawMove(canvas)
        } else {
            if (isCancel) {
                nextBitmap = curBitmap.copy(Bitmap.Config.RGB_565, true)
            }
            drawStatic(canvas)
        }
    }

    private fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            canvas.drawBitmap(curBitmap, 0f, 0f, null)
        } else {
            canvas.drawBitmap(nextBitmap, 0f, 0f, null)
        }
    }

    abstract fun drawMove(canvas: Canvas)

    /**
     * 滚动动画
     * 必须放在computeScroll()方法中执行
     */
    fun scrollAnim() {
        if (scroller.computeScrollOffset()) {
            val x = scroller.currX
            val y = scroller.currY
            setTouchPoint(x.toFloat(), y.toFloat())
            if (scroller.finalX == x && scroller.finalY == y) {
                isRunning = false
            }
            view?.postInvalidate()
        }
    }

    fun abortAnim() {
        if (!scroller.isFinished) {
            scroller.abortAnimation()
            isRunning = false
            setTouchPoint(scroller.finalX.toFloat(), scroller.finalY.toFloat())
            view?.postInvalidate()
        }
    }

    enum class Direction {
        NONE, NEXT, PRE
    }

    interface OnPageChangeListener {
        fun hasPrev(): Boolean
        operator fun hasNext(): Boolean
        fun pageCancel()
    }
}
