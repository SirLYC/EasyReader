package com.lyc.base

import android.app.Application
import android.content.Context
import android.os.StrictMode

/**
 * Created by Liu Yuchuan on 2020/1/7.
 */
class ReaderApplication : Application() {
    companion object {
        private lateinit var context: Context

        fun appContext() = context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        StrictMode.enableDefaults()
    }
}