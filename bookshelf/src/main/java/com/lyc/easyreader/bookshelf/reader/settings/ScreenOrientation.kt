package com.lyc.easyreader.bookshelf.reader.settings

import android.content.pm.ActivityInfo

/**
 * Created by Liu Yuchuan on 2020/2/6.
 */
enum class ScreenOrientation(
    val orientationValue: Int,
    val displayName: String
) {
    PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, "竖屏"),
    SYSTEM(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, "跟随系统"),
    LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, "横屏"),
    LANDSCAPE_REVERSE(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, "横屏反向")
}
