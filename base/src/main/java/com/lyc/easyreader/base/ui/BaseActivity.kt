package com.lyc.easyreader.base.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.lyc.easyreader.base.BuildConfig
import com.lyc.easyreader.base.ui.theme.NightModeManager
import com.lyc.easyreader.base.ui.theme.NightModeManager.NIGHT_MODE_MASK_COLOR
import com.lyc.easyreader.base.ui.theme.color_bg
import com.lyc.easyreader.base.utils.notch.NotchTools

/**
 * Created by Liu Yuchuan on 2020/1/8.
 */
abstract class BaseActivity : AppCompatActivity(), NightModeManager.INightModeChangeListener {

    companion object {
        private const val KEY_CONFIG_CHANGE = "KEY_CONFIG_CHANGE"
    }

    lateinit var rootView: FrameLayout
    private var createRootView = false
    private var maskView: View? = null
    var isCreateFromConfigChange: Boolean = false
        private set

    override fun onNightModeChange(enable: Boolean) {
        if (!createRootView) {
            return
        }

        if (!enable) {
            maskView?.isVisible = false
        } else {
            val maskView = this.maskView ?: View(this).apply {
                setBackgroundColor(NIGHT_MODE_MASK_COLOR)
                (window.decorView as? ViewGroup)?.addView(this)
            }
            maskView.bringToFront()
            maskView.isVisible = true
        }
    }

    fun enterFullScreen() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun exitFullScreen() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        isCreateFromConfigChange = savedInstanceState?.getBoolean(KEY_CONFIG_CHANGE, false) == true
        beforeOnCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
        beforeBaseOnCreate(savedInstanceState)
        if (BuildConfig.FORCE_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        NightModeManager.addNightModeChangeListener(this)
        window.let { window ->
            NotchTools.getFullScreenTools().fullScreenUseStatus(this)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
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
        rootView.setBackgroundColor(color_bg)
        afterBaseOnCreate(savedInstanceState, rootView)
        createRootView = true
        onNightModeChange(NightModeManager.nightModeEnable)
        setContentView(rootView)
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_CONFIG_CHANGE, isChangingConfigurations)
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
    open fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {

    }

    open fun fragmentHandleActivityRequestFirst() = true

    open fun fragmentHandlePermissionFirst() = true

    final override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (((requestCode shr 16) and 0xffff) != 0) {
            // 已经由父方法被Fragment消费了
            return
        }
        val fragmentFirst = fragmentHandleActivityRequestFirst()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (((requestCode shr 16) and 0xffff) != 0) {
            // 已经由父方法被Fragment消费了
            return
        }
        val fragmentFirst = fragmentHandlePermissionFirst()
        if (!fragmentFirst && handlePermissionResult(
                requestCode,
                permissions,
                grantResults
            )
        ) {
            return
        }
        for (fragment in supportFragmentManager.fragments) {
            if ((fragment as? BaseFragment)?.handlePermissionResult(
                    requestCode,
                    permissions,
                    grantResults
                ) == true
            ) {
                return
            }
        }

        if (fragmentFirst) {
            handlePermissionResult(requestCode, permissions, grantResults)
        }
    }

    open fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return false
    }

    open fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        return false
    }

    override fun onDestroy() {
        NightModeManager.removeNightModeChangeListener(this)
        super.onDestroy()
    }
}
