package com.lyc.easyreader

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import com.lyc.easyreader.base.app.ActivityCollector
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.getDrawableRes

/**
 * Created by Liu Yuchuan on 2020/2/6.
 */
class SplashActivity : BaseActivity() {
    companion object {
        private var showSplash = false
    }

    private val handler = Handler()

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        super.afterBaseOnCreate(savedInstanceState, rootView)

        if (showSplash && ActivityCollector.instance.hasCreatedActivityExcept(this)) {
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
            overridePendingTransition(0, R.anim.splash_fade_out)
        }, 1500)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
