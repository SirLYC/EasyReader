package com.lyc.base.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Liu Yuchuan on 2020/1/8.
 */
abstract class BaseActivity : AppCompatActivity() {

    final override fun onCreate(savedInstanceState: Bundle?) {
        beforeOnCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
        beforeBaseOnCreate(savedInstanceState)
        window.let { window ->
            if (Build.VERSION.SDK_INT >= 23) {
                // 21就可以设置这个flag了
                // 但是23才能改变文字颜色
                // 如果21、22的设备设置了这个flag，但是无法改变文字颜色，状态栏会看不见的
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = Color.TRANSPARENT
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        afterBaseOnCreate(savedInstanceState)
    }

    /**
     * [AppCompatActivity.onCreate] 之前
     */
    open fun beforeOnCreate(savedInstanceState: Bundle?) {

    }

    /**
     * [BaseActivity.onCreate]中紧接着[AppCompatActivity.onCreate]调用
     */
    open fun beforeBaseOnCreate(savedInstanceState: Bundle?) {

    }

    /**
     * [BaseActivity.onCreate]末尾调用
     */
    open fun afterBaseOnCreate(savedInstanceState: Bundle?) {

    }
}
