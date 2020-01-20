package com.lyc.base.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.lyc.base.ui.theme.NightModeManager

/**
 * Created by Liu Yuchuan on 2020/1/8.
 */
abstract class BaseActivity : AppCompatActivity(), NightModeManager.INightModeChangeListener {

    companion object {
        const val NIGHT_MODE_MASK_COLOR = 0x7F000000
    }

    lateinit var rootView: FrameLayout
    private var createRootView = false
    private var maskView: View? = null

    override fun onNightModeChange(enable: Boolean) {
        if (!createRootView) {
            return
        }

        if (!enable) {
            maskView?.isVisible = false
        } else {
            val maskView = this.maskView ?: View(this).apply {
                setBackgroundColor(NIGHT_MODE_MASK_COLOR)
                this@BaseActivity.rootView.addView(this)
            }
            maskView.bringToFront()
            maskView.isVisible = true
        }
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        beforeOnCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
        beforeBaseOnCreate(savedInstanceState)
        NightModeManager.addNightModeChangeListener(this)
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
        rootView = FrameLayout(this)
        afterBaseOnCreate(savedInstanceState, rootView)
        createRootView = true
        onNightModeChange(NightModeManager.nightModeEnable)
        setContentView(rootView)
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
    open fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: ViewGroup) {

    }

    open fun fragmentHandleRequestFirst() = true

    final override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragmentFirst = fragmentHandleRequestFirst()
        if (!fragmentFirst && handleActivityResult(
                requestCode,
                resultCode,
                data
            )
        ) {
            return
        }
        for (fragment in supportFragmentManager.fragments) {
            if ((fragment as? BaseFragment)?.handleActivityResult(
                    requestCode,
                    resultCode,
                    data
                ) == true
            ) {
                return
            }
        }

        if (fragmentFirst) {
            handleActivityResult(requestCode, resultCode, data)
        }
    }

    open fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return false
    }

    override fun onDestroy() {
        NightModeManager.removeNightModeChangeListener(this)
        super.onDestroy()
    }
}
