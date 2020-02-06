package com.lyc.easyreader.lauch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import com.lyc.easyreader.MainActivity
import com.lyc.easyreader.R
import com.lyc.easyreader.base.app.ActivityCollector
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/2/6.
 */
class SplashActivity : BaseActivity() {
    companion object {
        private var showSplash = false
        const val TAG = "SplashActivity"
    }

    private val handler = Handler()

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        super.afterBaseOnCreate(savedInstanceState, rootView)

        if (showSplash) {
            if (!ActivityCollector.instance.hasCreatedActivityExcept(this)) {
                LogUtils.d(TAG, "No other activity except SplashActivity...Just start MainActivity")
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                LogUtils.d(TAG, "User click icon to enter app...Don't show splash this time.")
            }
            finish()
            return
        }

        showSplash = true
        enterFullscreen()
        ImageView(this).apply {
            setImageDrawable(getDrawableRes(R.mipmap.splash))
            rootView.addView(this, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            })
        }

        handler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
