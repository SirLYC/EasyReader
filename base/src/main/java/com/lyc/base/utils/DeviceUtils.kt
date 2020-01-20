package com.lyc.base.utils

import android.os.Build
import android.view.View
import android.view.Window
import com.lyc.base.ReaderApplication
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/1/8.
 */
private const val TAG = "DeviceUtils"

private val statusBarHeightLock = ReentrantLock()
@Volatile
private var statusBarHeightCache = -1

fun statusBarHeight(forceRetrieve: Boolean = false): Int {
    var result = statusBarHeightCache
    if (forceRetrieve || result <= 0) {
        val resources = ReaderApplication.appContext().resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        } else {
            LogUtils.e(TAG, "getStatusBarHeight resourceId=$resourceId")
        }
        statusBarHeightLock.withLock {
            if (forceRetrieve || statusBarHeightCache <= 0) {
                statusBarHeightCache = result
            }
        }
    }

    return result
}

fun Window?.statusBarBlackText(isBlack: Boolean) {
    if (Build.VERSION.SDK_INT >= 23) {
        this?.let { window ->
            window.decorView.systemUiVisibility =
                if (isBlack) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}

fun dp2px(dpVal: Int): Int {
    val resources = ReaderApplication.appContext().resources
    return (dpVal * resources.displayMetrics.density).roundToInt()
}

fun dp2pxf(dpVal: Float): Float {
    val resources = ReaderApplication.appContext().resources
    return dpVal * resources.displayMetrics.density
}

fun px2dp(dpVal: Int): Int {
    val resources = ReaderApplication.appContext().resources
    return (dpVal / resources.displayMetrics.density).roundToInt()
}

fun px2dpf(dpVal: Float): Float {
    val resources = ReaderApplication.appContext().resources
    return dpVal / resources.displayMetrics.density
}

fun sp2px(spVal: Int): Int {
    val resources = ReaderApplication.appContext().resources
    return (spVal * resources.displayMetrics.scaledDensity).roundToInt()
}

fun sp2pxf(spVal: Float): Float {
    val resources = ReaderApplication.appContext().resources
    return spVal * resources.displayMetrics.scaledDensity
}