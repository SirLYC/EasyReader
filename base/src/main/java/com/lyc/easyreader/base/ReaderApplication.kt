package com.lyc.easyreader.base

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.lyc.easyreader.base.app.IApplicationOnCreateListener
import com.lyc.easyreader.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/1/7.
 */
class ReaderApplication : Application() {
    companion object {

        private const val TAG = "ReaderApplication"

        private lateinit var context: Context

        fun appContext() =
            context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        StrictMode.setVmPolicy(VmPolicy.Builder().detectAll().penaltyLog().build())
        LogUtils.startTiming("Application Querying extensions")
        val extensions =
            getAppExtensions<IApplicationOnCreateListener>()
        LogUtils.debugLogTiming(
            TAG,
            "App finish query IApplicationOnCreateListener extensions.",
            "Application Querying extensions"
        )
        extensions.forEach {
            LogUtils.startTiming("onAppCreate")
            it.onAppCreate(this)
            LogUtils.debugLogTiming(TAG, "${it.javaClass.name}#onAppCreate called", "onAppCreate")
        }
    }
}