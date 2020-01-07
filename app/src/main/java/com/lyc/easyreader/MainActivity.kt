package com.lyc.easyreader

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lyc.base.log.LogUtils

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
