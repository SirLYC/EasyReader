package com.lyc.easyreader

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import com.lyc.base.ui.BaseActivity
import com.lyc.base.utils.LogUtils

class MainActivity : BaseActivity() {

    companion object {
        const val TAG = "MainActivity"

        const val FRAGMENT_CONTAINER_ID = 1
    }

    private lateinit var container: FrameLayout

    override fun afterBaseOnCreate(savedInstanceState: Bundle?) {
        container = FrameLayout(this)
        container.id = FRAGMENT_CONTAINER_ID
        setContentView(container)

        container = FrameLayout(this)
        container.id = FRAGMENT_CONTAINER_ID
        container.setBackgroundColor(Color.BLACK)

        setContentView(container)

        LogUtils.d(
            TAG,
            "[MainActivity] OnCreate...bundle=${savedInstanceState}, intent.data=${intent?.data}"
        )
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        LogUtils.d(
            TAG,
            "[MainActivity] onNewIntent...new intent.data=${intent?.data}"
        )
    }
}
