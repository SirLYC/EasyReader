package com.lyc.base.app

import android.app.Application
import com.lyc.appinject.annotations.ExtensionImpl
import com.lyc.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@ExtensionImpl(extension = IApplicationOnCreateListener::class)
internal class AppCrashHandler : Thread.UncaughtExceptionHandler, IApplicationOnCreateListener {

    companion object {
        const val TAG = "AppCrashHandler"
    }

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        LogUtils.e(TAG, "App Crash! Thread=$t", e)
        defaultHandler?.uncaughtException(t, e)
    }

    override fun onAppCreate(application: Application) {
        AppCrashHandler()
    }
}