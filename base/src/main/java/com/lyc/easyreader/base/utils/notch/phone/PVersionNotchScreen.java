package com.lyc.easyreader.base.utils.notch.phone;

import android.app.Activity;
import android.view.DisplayCutout;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.lyc.easyreader.base.utils.notch.core.AbsNotchScreenSupport;
import com.lyc.easyreader.base.utils.notch.core.OnNotchCallBack;


/**
 * targetApi>=28才能使用API，有的手机厂商在P上会放弃O适配方案，暂时针对P手机不做特殊处理
 *
 * @author zhangzhun
 * @date 2018/11/5
 */

public class PVersionNotchScreen extends AbsNotchScreenSupport {

    @RequiresApi(api = 28)
    @Override
    public boolean isNotchScreen(Window window) {
        WindowInsets windowInsets = window.getDecorView().getRootWindowInsets();
        if (windowInsets == null) {
            return false;
        }

        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        return displayCutout != null && displayCutout.getBoundingRects() != null;
    }

    @RequiresApi(api = 28)
    @Override
    public int getNotchHeight(Window window) {
        int notchHeight = 0;
        WindowInsets windowInsets = window.getDecorView().getRootWindowInsets();
        if (windowInsets == null) {
            return 0;
        }

        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        if (displayCutout == null || displayCutout.getBoundingRects() == null) {
            return 0;
        }

        notchHeight = displayCutout.getSafeInsetTop();

        return notchHeight;
    }

    @RequiresApi(api = 28)
    @Override
    public void fullScreenDontUseStatus(final Activity activity, OnNotchCallBack notchCallBack) {
        super.fullScreenDontUseStatus(activity, notchCallBack);
        if (!isNotchScreen(activity.getWindow())) {
            return;
        }
        WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
        attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
        activity.getWindow().setAttributes(attributes);
    }

    @RequiresApi(api = 28)
    @Override
    public void fullScreenUseStatus(Activity activity, OnNotchCallBack notchCallBack) {
        super.fullScreenUseStatus(activity, notchCallBack);
        WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
        attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        activity.getWindow().setAttributes(attributes);
    }
}
