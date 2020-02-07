package com.lyc.easyreader.bookshelf.reader.page.anim

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import com.lyc.easyreader.bookshelf.utils.getCross
import kotlin.math.*

/**
 * Created by Liu Yuchuan on 2020/2/5.
 */
class SimulationPageAnim(
    w: Int,
    h: Int,
    view: View?,
    listener: OnPageChangeListener?
) : PageAnimation(w, h, view, listener!!) {
    // 左下方的点
    private val bezierStart1 = PointF() // 贝塞尔曲线起始点
    private val bezierControl1 = PointF() // 贝塞尔曲线控制点
    private val bezierVertex1 = PointF() // 贝塞尔曲线顶点
    private var bezierEnd1: PointF? = PointF() // 贝塞尔曲线结束点
    private val bezierStart2 = PointF() // 另一条贝塞尔曲线
    private val bezierControl2 = PointF()
    private val bezierVertex2 = PointF()
    private var bezierEnd2: PointF? = PointF()
    private var degrees = 0f
    private var touchToCornerDis = 0f
    private val colorMatrixFilter: ColorMatrixColorFilter
    private val matrix: Matrix
    private val matrixArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 1.0f)
    private var isRTOrLB = false // 是否属于右上左下
    private var backShadowDrawableLR // 有阴影的GradientDrawable
            : GradientDrawable? = null
    private lateinit var backShadowDrawableRL: GradientDrawable
    private lateinit var folderShadowDrawableLR: GradientDrawable
    private lateinit var folderShadowDrawableRL: GradientDrawable
    private lateinit var frontShadowDrawableHBT: GradientDrawable
    private lateinit var frontShadowDrawableHTB: GradientDrawable
    private lateinit var frontShadowDrawableVLR: GradientDrawable
    private lateinit var frontShadowDrawableVRL: GradientDrawable
    private val paint = Paint()
    // 拖拽点对应的页脚
    private var cornerX = 1
    private var cornerY = 1
    // 翻页和下边部分与拖拽页脚形成区域的Path
    private val path0 = Path()
    private val path1 = Path()
    private val pathXorResult = Path()
    private val maxLength = hypot(screenWidth.toDouble(), screenHeight.toDouble()).toFloat()

    companion object {
        private const val TAG = "SimulationPageAnim"
    }

    init {
        paint.style = Paint.Style.FILL
        createDrawable()
        val cm = ColorMatrix() //设置颜色数组
        cm.set(FloatArray(20) { if (it % 6 == 0) 1f else 0f })
        colorMatrixFilter = ColorMatrixColorFilter(cm)
        matrix = Matrix()
        touchX = 0.01f // 不让x,y为0,否则在点计算时会有问题
        touchY = 0.01f
    }

    override fun drawMove(canvas: Canvas) {
        if (direction === Direction.NEXT) {
            calcPoints()
            updatePath0()
            drawCurrentPageArea(canvas, curBitmap)
            drawNextPageAreaAndShadow(canvas, nextBitmap)
            drawCurrentPageShadow(canvas)
            drawCurrentBackArea(canvas, curBitmap)
        } else {
            calcPoints()
            updatePath0()
            drawCurrentPageArea(canvas, nextBitmap)
            drawNextPageAreaAndShadow(canvas, curBitmap)
            drawCurrentPageShadow(canvas)
            drawCurrentBackArea(canvas, nextBitmap)
        }
    }

    override fun startAnimImp() {
        var dx: Int
        val dy: Int
        val direction =
            direction
        // dx 水平方向滑动的距离，负值会使滚动向左滚动
        // dy 垂直方向滑动的距离，负值会使滚动向上滚动
        if (isCancel) {
            dx = if (cornerX > 0 && direction == Direction.NEXT) {
                (screenWidth - touchX).toInt()
            } else {
                (-touchX).toInt()
            }
            if (direction != Direction.NEXT) {
                dx = (-(screenWidth + touchX)).toInt()
            }
            dy = if (cornerY > 0) {
                (screenHeight - touchY).toInt()
            } else {
                (-touchY).toInt() // 防止mTouchY最终变为0
            }
        } else {
            dx = if (cornerX > 0 && direction == Direction.NEXT) {
                (-(screenWidth + touchX)).toInt()
            } else {
                (screenWidth - touchX + screenWidth).toInt()
            }
            dy = if (cornerY > 0) {
                (screenHeight - touchY).toInt()
            } else {
                (1 - touchY).toInt() // 防止mTouchY最终变为0
            }
        }
        scroller.startScroll(touchX.toInt(), touchY.toInt(), dx, dy, 400)
    }

    //上一页滑动不出现对角
    override var direction: Direction
        get() = super.direction
        set(direction) {
            super.direction = direction
            if (direction == Direction.PRE) {
                //上一页滑动不出现对角
                if (2 * startX > screenWidth) {
                    calcCornerXY(startX, screenHeight.toFloat())
                } else {
                    calcCornerXY(screenWidth - startX, screenHeight.toFloat())
                }
            } else {
                if (screenWidth > startX * 2) {
                    calcCornerXY(screenWidth - startX, startY)
                }
            }
        }

    override fun setStartPoint(x: Float, y: Float) {
        super.setStartPoint(x, y)
        calcCornerXY(x, y)
    }

    override fun setTouchPoint(x: Float, y: Float) {
        super.setTouchPoint(x, y)
        //触摸y中间位置吧y变成屏幕高度
        if (startY * 3 > screenHeight && startY * 3 < screenHeight * 2 || direction == Direction.PRE) {
            touchY = screenHeight.toFloat()
        }
        if (startY * 3 > screenHeight && startY * 2 < screenHeight && direction == Direction.NEXT) {
            touchY = 1f
        }
    }

    /**
     * 创建阴影的GradientDrawable
     */
    private fun createDrawable() {
        val color = intArrayOf(0x333333, -0x4fcccccd)
        folderShadowDrawableRL = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT, color
        )
        folderShadowDrawableRL.gradientType = GradientDrawable.LINEAR_GRADIENT
        folderShadowDrawableLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, color
        )
        folderShadowDrawableLR.gradientType = GradientDrawable.LINEAR_GRADIENT
        // 背面颜色组
        val backShadowColors = intArrayOf(-0xeeeeef, 0x111111)
        backShadowDrawableRL = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT, backShadowColors
        )
        backShadowDrawableRL.gradientType = GradientDrawable.LINEAR_GRADIENT
        backShadowDrawableLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, backShadowColors
        )
        backShadowDrawableLR!!.gradientType = GradientDrawable.LINEAR_GRADIENT
        // 前面颜色组
        val frontShadowColors = intArrayOf(-0x7feeeeef, 0x111111)
        frontShadowDrawableVLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, frontShadowColors
        )
        frontShadowDrawableVLR.gradientType = GradientDrawable.LINEAR_GRADIENT
        frontShadowDrawableVRL = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT, frontShadowColors
        )
        frontShadowDrawableVRL.gradientType = GradientDrawable.LINEAR_GRADIENT
        frontShadowDrawableHTB = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, frontShadowColors
        )
        frontShadowDrawableHTB.gradientType = GradientDrawable.LINEAR_GRADIENT
        frontShadowDrawableHBT =
            GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, frontShadowColors)
        frontShadowDrawableHBT.gradientType = GradientDrawable.LINEAR_GRADIENT
    }

    /**
     * 绘制翻起页背面
     */
    private fun drawCurrentBackArea(canvas: Canvas, bitmap: Bitmap) {
        val i = (bezierStart1.x + bezierControl1.x).toInt() / 2
        val f1 = abs(i - bezierControl1.x)
        val i1 = (bezierStart2.y + bezierControl2.y).toInt() / 2
        val f2 = abs(i1 - bezierControl2.y)
        val f3 = f1.coerceAtMost(f2)
        path1.reset()
        path1.moveTo(bezierVertex2.x, bezierVertex2.y)
        path1.lineTo(bezierVertex1.x, bezierVertex1.y)
        path1.lineTo(bezierEnd1!!.x, bezierEnd1!!.y)
        path1.lineTo(touchX, touchY)
        path1.lineTo(bezierEnd2!!.x, bezierEnd2!!.y)
        path1.close()
        val folderShadowDrawable: GradientDrawable?
        val left: Int
        val right: Int
        if (isRTOrLB) {
            left = (bezierStart1.x - 1).toInt()
            right = (bezierStart1.x + f3 + 1).toInt()
            folderShadowDrawable = folderShadowDrawableLR
        } else {
            left = (bezierStart1.x - f3 - 1).toInt()
            right = (bezierStart1.x + 1).toInt()
            folderShadowDrawable = folderShadowDrawableRL
        }
        canvas.save()
        canvas.clipPath(path0)
        canvas.clipPath(path1)
        paint.colorFilter = colorMatrixFilter
        //对Bitmap进行取色
        val color = bitmap.getPixel(1, 1)
        //获取对应的三色
        val red = color and 0xff0000 shr 16
        val green = color and 0x00ff00 shr 8
        val blue = color and 0x0000ff
        //转换成含有透明度的颜色
        val tempColor = Color.argb(200, red, green, blue)
        val dis = hypot(
            cornerX - bezierControl1.x.toDouble(),
            bezierControl2.y - cornerY.toDouble()
        ).toFloat()
        val f8 = (cornerX - bezierControl1.x) / dis
        val f9 = (bezierControl2.y - cornerY) / dis
        matrixArray[0] = 1 - 2 * f9 * f9
        matrixArray[1] = 2 * f8 * f9
        matrixArray[3] = matrixArray[1]
        matrixArray[4] = 1 - 2 * f8 * f8
        matrix.reset()
        matrix.setValues(matrixArray)
        matrix.preTranslate(-bezierControl1.x, -bezierControl1.y)
        matrix.postTranslate(bezierControl1.x, bezierControl1.y)
        canvas.drawBitmap(bitmap, matrix, paint)
        //背景叠加
        canvas.drawColor(tempColor)
        paint.colorFilter = null
        canvas.rotate(degrees, bezierStart1.x, bezierStart1.y)
        folderShadowDrawable.setBounds(
            left, bezierStart1.y.toInt(), right,
            (bezierStart1.y + maxLength).toInt()
        )
        folderShadowDrawable.draw(canvas)
        canvas.restore()
    }

    /**
     * 绘制翻起页的阴影
     */
    private fun drawCurrentPageShadow(canvas: Canvas) {
        val degree = if (isRTOrLB) {
            (Math.PI
                    / 4
                    - atan2(
                bezierControl1.y - touchY.toDouble(), touchX
                        - bezierControl1.x.toDouble()
            ))
        } else {
            (Math.PI
                    / 4
                    - atan2(
                touchY - bezierControl1.y.toDouble(), touchX
                        - bezierControl1.x.toDouble()
            ))
        }
        // 翻起页阴影顶点与touch点的距离
        val d1 = 25.toFloat() * 1.414 * cos(degree)
        val d2 = 25.toFloat() * 1.414 * sin(degree)
        val x = (touchX + d1).toFloat()
        val y: Float
        y = if (isRTOrLB) {
            (touchY + d2).toFloat()
        } else {
            (touchY - d2).toFloat()
        }
        path1.reset()
        path1.moveTo(x, y)
        path1.lineTo(touchX, touchY)
        path1.lineTo(bezierControl1.x, bezierControl1.y)
        path1.lineTo(bezierStart1.x, bezierStart1.y)
        path1.close()
        canvas.save()
        xorClipPath(canvas, path0)
        canvas.clipPath(path1)
        var leftx: Int
        var rightx: Int
        var mCurrentPageShadow: GradientDrawable?
        if (isRTOrLB) {
            leftx = bezierControl1.x.toInt()
            rightx = bezierControl1.x.toInt() + 25
            mCurrentPageShadow = frontShadowDrawableVLR
        } else {
            leftx = (bezierControl1.x - 25).toInt()
            rightx = bezierControl1.x.toInt() + 1
            mCurrentPageShadow = frontShadowDrawableVRL
        }
        var rotateDegrees = Math.toDegrees(
            atan2(
                (touchX - bezierControl1.x).toDouble(),
                bezierControl1.y - touchY.toDouble()
            )
        ).toFloat()
        canvas.rotate(rotateDegrees, bezierControl1.x, bezierControl1.y)
        mCurrentPageShadow.setBounds(
            leftx,
            (bezierControl1.y - maxLength).toInt(), rightx,
            bezierControl1.y.toInt()
        )
        mCurrentPageShadow.draw(canvas)
        canvas.restore()
        path1.reset()
        path1.moveTo(x, y)
        path1.lineTo(touchX, touchY)
        path1.lineTo(bezierControl2.x, bezierControl2.y)
        path1.lineTo(bezierStart2.x, bezierStart2.y)
        path1.close()
        canvas.save()
        xorClipPath(canvas, path0)
        canvas.clipPath(path1)
        if (isRTOrLB) {
            leftx = bezierControl2.y.toInt()
            rightx = (bezierControl2.y + 25).toInt()
            mCurrentPageShadow = frontShadowDrawableHTB
        } else {
            leftx = (bezierControl2.y - 25).toInt()
            rightx = (bezierControl2.y + 1).toInt()
            mCurrentPageShadow = frontShadowDrawableHBT
        }
        rotateDegrees = Math.toDegrees(
            atan2(
                (bezierControl2.y
                        - touchY).toDouble(), bezierControl2.x - touchX.toDouble()
            )
        ).toFloat()
        canvas.rotate(rotateDegrees, bezierControl2.x, bezierControl2.y)
        val temp = if (bezierControl2.y < 0) bezierControl2.y - screenHeight else bezierControl2.y
        val hmg = hypot(bezierControl2.x.toDouble(), temp.toDouble()).toInt()
        if (hmg > maxLength) mCurrentPageShadow
            .setBounds(
                (bezierControl2.x - 25).toInt() - hmg, leftx,
                (bezierControl2.x + maxLength).toInt() - hmg,
                rightx
            ) else mCurrentPageShadow.setBounds(
            (bezierControl2.x - maxLength).toInt(), leftx,
            bezierControl2.x.toInt(), rightx
        )
        mCurrentPageShadow.draw(canvas)
        canvas.restore()
    }

    private fun xorClipPath(
        canvas: Canvas,
        path: Path
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pathXorResult.reset()
            pathXorResult.moveTo(0f, 0f)
            pathXorResult.lineTo(screenWidth.toFloat(), 0f)
            pathXorResult.lineTo(screenWidth.toFloat(), screenHeight.toFloat())
            pathXorResult.lineTo(0f, screenHeight.toFloat())
            pathXorResult.close()
            pathXorResult.op(path, Path.Op.XOR)
            canvas.clipPath(pathXorResult)
        } else {
            @Suppress("DEPRECATION")
            canvas.clipPath(path, Region.Op.XOR)
        }
    }

    private fun drawNextPageAreaAndShadow(
        canvas: Canvas,
        bitmap: Bitmap
    ) {
        path1.reset()
        path1.moveTo(bezierStart1.x, bezierStart1.y)
        path1.lineTo(bezierVertex1.x, bezierVertex1.y)
        path1.lineTo(bezierVertex2.x, bezierVertex2.y)
        path1.lineTo(bezierStart2.x, bezierStart2.y)
        path1.lineTo(cornerX.toFloat(), cornerY.toFloat())
        path1.close()
        degrees = Math.toDegrees(
            atan2(
                (bezierControl1.x
                        - cornerX).toDouble(), bezierControl2.y - cornerY.toDouble()
            )
        ).toFloat()
        val leftX: Int
        val rightX: Int
        val backShadowDrawable: GradientDrawable?
        if (isRTOrLB) { //左下及右上
            leftX = bezierStart1.x.toInt()
            rightX = (bezierStart1.x + touchToCornerDis / 4).toInt()
            backShadowDrawable = backShadowDrawableLR
        } else {
            leftX = (bezierStart1.x - touchToCornerDis / 4).toInt()
            rightX = bezierStart1.x.toInt()
            backShadowDrawable = backShadowDrawableRL
        }
        canvas.save()
        canvas.clipPath(path0)
        canvas.clipPath(path1)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.rotate(degrees, bezierStart1.x, bezierStart1.y)
        backShadowDrawable!!.setBounds(
            leftX, bezierStart1.y.toInt(), rightX,
            (maxLength + bezierStart1.y).roundToInt()
        ) //左上及右下角的xy坐标值,构成一个矩形
        backShadowDrawable.draw(canvas)
        canvas.restore()
    }

    private fun drawCurrentPageArea(canvas: Canvas, bitmap: Bitmap) {
        canvas.save()
        xorClipPath(canvas, path0)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.restore()
    }

    private fun updatePath0() {
        path0.reset()
        path0.moveTo(bezierStart1.x, bezierStart1.y)
        path0.quadTo(
            bezierControl1.x, bezierControl1.y, bezierEnd1!!.x,
            bezierEnd1!!.y
        )
        path0.lineTo(touchX, touchY)
        path0.lineTo(bezierEnd2!!.x, bezierEnd2!!.y)
        path0.quadTo(
            bezierControl2.x, bezierControl2.y, bezierStart2.x,
            bezierStart2.y
        )
        path0.lineTo(cornerX.toFloat(), cornerY.toFloat())
        path0.close()
    }

    /**
     * 计算拖拽点对应的拖拽脚
     */
    private fun calcCornerXY(x: Float, y: Float) {
        cornerX = if (x * 2 <= screenWidth) {
            0
        } else {
            screenWidth
        }
        cornerY = if (y * 2 <= screenHeight) {
            0
        } else {
            screenHeight
        }
        isRTOrLB = (cornerX == 0 && cornerY == screenHeight
                || cornerX == screenWidth && cornerY == 0)
    }

    private fun calcPoints() {
        var middleX = (touchX + cornerX) / 2
        var middleY = (touchY + cornerY) / 2
        bezierControl1.x = middleX - (cornerY - middleY) * (cornerY - middleY) / (cornerX - middleX)
        bezierControl1.y = cornerY.toFloat()
        bezierControl2.x = cornerX.toFloat()
        val f4 = cornerY - middleY
        if (f4 == 0f) {
            bezierControl2.y = middleY - (cornerX - middleX) * (cornerX - middleX) / 0.1f
        } else {
            bezierControl2.y =
                middleY - (cornerX - middleX) * (cornerX - middleX) / (cornerY - middleY)
        }
        bezierStart1.x = bezierControl1.x - (cornerX - bezierControl1.x) / 2
        bezierStart1.y = cornerY.toFloat()
        // 当mBezierStart1.x < 0或者mBezierStart1.x > width时
        // 如果继续翻页，会出现BUG故在此限制
        if (touchX > 0 && touchX < screenWidth) {
            if (bezierStart1.x < 0 || bezierStart1.x > screenWidth) {
                if (bezierStart1.x < 0) {
                    bezierStart1.x = screenWidth - bezierStart1.x
                }
                val f1 = abs(cornerX - touchX)
                val f2 = screenWidth * f1 / bezierStart1.x
                touchX = abs(cornerX - f2)
                val f3 = abs(cornerX - touchX) * abs(cornerY - touchY) / f1
                touchY = abs(cornerY - f3)
                middleX = (touchX + cornerX) / 2
                middleY = (touchY + cornerY) / 2
                bezierControl1.x =
                    middleX - (cornerY - middleY) * (cornerY - middleY) / (cornerX - middleX)
                bezierControl1.y = cornerY.toFloat()
                bezierControl2.x = cornerX.toFloat()
                val f5 = cornerY - middleY
                if (f5 == 0f) {
                    bezierControl2.y = middleY - (cornerX - middleX) * (cornerX - middleX) / 0.1f
                } else {
                    bezierControl2.y =
                        middleY - (cornerX - middleX) * (cornerX - middleX) / (cornerY - middleY)
                }
                bezierStart1.x = (bezierControl1.x
                        - (cornerX - bezierControl1.x) / 2)
            }
        }
        bezierStart2.x = cornerX.toFloat()
        bezierStart2.y = bezierControl2.y - (cornerY - bezierControl2.y) / 2
        touchToCornerDis =
            hypot((touchX - cornerX).toDouble(), (touchY - cornerY).toDouble()).toFloat()
        bezierEnd1 = getCross(PointF(touchX, touchY), bezierControl1, bezierStart1, bezierStart2)
        bezierEnd2 = getCross(PointF(touchX, touchY), bezierControl2, bezierStart1, bezierStart2)
        if (bezierEnd1 != null) {
            bezierVertex1.x = (bezierStart1.x + 2 * bezierControl1.x + bezierEnd1!!.x) / 4
            bezierVertex1.y = (2 * bezierControl1.y + bezierStart1.y + bezierEnd1!!.y) / 4
        }
        if (bezierEnd2 != null) {
            bezierVertex2.x = (bezierStart2.x + 2 * bezierControl2.x + bezierEnd2!!.x) / 4
            bezierVertex2.y = (2 * bezierControl2.y + bezierStart2.y + bezierEnd2!!.y) / 4
        }
    }
}
