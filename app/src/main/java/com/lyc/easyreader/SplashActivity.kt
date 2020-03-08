package com.lyc.easyreader

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.*
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
        private const val DELAY = 1500L
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

        val anim = AnimationSet(true)
        val rotateAnim = RotateAnimation(
            180f,
            360f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotateAnim.duration = 800L
        rotateAnim.interpolator = DecelerateInterpolator()
        anim.addAnimation(rotateAnim)
        val scaleAnim1 = ScaleAnimation(
            0.5f,
            1.1f,
            0.5f,
            1.1f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        scaleAnim1.duration = 400L
        scaleAnim1.interpolator = DecelerateInterpolator()
        anim.addAnimation(scaleAnim1)

        val scaleAnim2 = ScaleAnimation(
            1.1f,
            1.0f,
            1.1f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        scaleAnim2.duration = 400L
        scaleAnim2.startOffset = 400L
        scaleAnim2.interpolator = LinearInterpolator()
        anim.addAnimation(scaleAnim2)
        anim.startOffset = 300L

        ImageView(this).apply {
            setImageDrawable(getDrawableRes(R.mipmap.splash))
            rootView.addView(this, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            })
            post {
                startAnimation(anim)
            }
        }

        handler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(0, R.anim.splash_fade_out)
        }, DELAY)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
