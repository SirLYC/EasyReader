package com.lyc.easyreader

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.lyc.easyreader.base.app.ActivityCollector
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.theme.color_primary_text
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.textSizeInPx

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
            setImageDrawable(getDrawableRes(R.mipmap.ic_launcher))
            rootView.addView(this, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            })
            post {
                startAnimation(anim)
            }
        }

        TextView(this).apply {
            setTextColor(color_primary_text)
            textSizeInPx = 54f
            rootView.addView(this, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM
                bottomMargin = dp2px(32)
            })
            gravity = Gravity.CENTER

            setTypeface(Typeface.createFromAsset(assets, "ttgb.ttf"), Typeface.BOLD)

            text = ("轻松阅 --By LYC")
            alpha = 0f
            post {
                animate().alpha(1f).setDuration(200L).setStartDelay(300L).start()
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
