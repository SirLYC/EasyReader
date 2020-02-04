package com.lyc.easyreader.bookshelf.reader.page;


import androidx.annotation.ColorInt;

/**
 * Created by Liu Yuchuan on 2020/2/2.
 */
public enum PageStyle {
    BG_0(0xFF2C2C2C, 0XFFCEC29C),
    BG_1(0xFF2F332D, 0XFFCCEBCC),
    BG_2(0XFF92918C, 0XFFAAAAAA),
    BG_3(0XFF383429, 0XFFD1CEC5),
    BG_4(0XFF627176, 0XFF001C27),
    NIGHT(0X99FFFFFF, 0XFF000000);

    private int fontColor;
    private int bgColor;

    PageStyle(@ColorInt int fontColor, @ColorInt int bgColor) {
        this.fontColor = fontColor;
        this.bgColor = bgColor;
    }

    public int getFontColor() {
        return fontColor;
    }

    public int getBgColor() {
        return bgColor;
    }
}
