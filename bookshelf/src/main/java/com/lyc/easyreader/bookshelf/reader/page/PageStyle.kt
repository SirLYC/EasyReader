package com.lyc.easyreader.bookshelf.reader.page

import androidx.annotation.ColorInt
import com.lyc.easyreader.base.ui.getColorRes
import com.lyc.easyreader.base.utils.isLightColor
import com.lyc.easyreader.bookshelf.R
import java.util.concurrent.atomic.AtomicInteger


/**
 * Created by Liu Yuchuan on 2020/2/2.
 */
class PageStyle private constructor(
    @ColorInt val fontColor: Int, @ColorInt val bgColor: Int, val id: Int
) {
    val statusBarBlack = isLightColor(bgColor)

    constructor(@ColorInt fontColor: Int, @ColorInt bgColor: Int) : this(
        fontColor,
        bgColor,
        idGen.getAndIncrement()
    )

    companion object {
        private val idGen = AtomicInteger(10)
        @JvmField
        val BG_1 =
            PageStyle(getColorRes(R.color.reader_font_1), getColorRes(R.color.reader_bg_1), 1)
        @JvmField
        val BG_2 =
            PageStyle(getColorRes(R.color.reader_font_2), getColorRes(R.color.reader_bg_2), 2)
        @JvmField
        val BG_3 =
            PageStyle(getColorRes(R.color.reader_font_3), getColorRes(R.color.reader_bg_3), 3)
        @JvmField
        val BG_4 =
            PageStyle(getColorRes(R.color.reader_font_4), getColorRes(R.color.reader_bg_4), 4)
        @JvmField
        val BG_5 =
            PageStyle(getColorRes(R.color.reader_font_5), getColorRes(R.color.reader_bg_5), 5)
        @JvmField
        val BG_6 =
            PageStyle(getColorRes(R.color.reader_font_6), getColorRes(R.color.reader_bg_6), 6)
        @JvmField
        val BG_7 =
            PageStyle(getColorRes(R.color.reader_font_7), getColorRes(R.color.reader_bg_7), 7)
        @JvmField
        val BG_8 =
            PageStyle(getColorRes(R.color.reader_font_8), getColorRes(R.color.reader_bg_8), 8)
        @JvmField
        val BG_9 =
            PageStyle(getColorRes(R.color.reader_font_9), getColorRes(R.color.reader_bg_9), 9)

        @JvmField
        val NIGHT = PageStyle(0XFFFFFFFF.toInt(), 0XFF000000.toInt(), 5)

        val innerStyles = mapOf(
            Pair(BG_1.id, BG_1),
            Pair(BG_2.id, BG_2),
            Pair(BG_3.id, BG_3),
            Pair(BG_4.id, BG_4),
            Pair(BG_5.id, BG_5),
            Pair(BG_6.id, BG_6),
            Pair(BG_7.id, BG_7),
            Pair(BG_8.id, BG_8),
            Pair(BG_9.id, BG_9)
        )


    }
}
