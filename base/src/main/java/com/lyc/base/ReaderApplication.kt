package com.lyc.base

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.os.SystemClock
import com.lyc.appinject.ModuleApi
import com.lyc.base.app.IApplicationOnCreateListener
import com.lyc.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/1/7.
 */
class ReaderApplication : Application() {
    companion object {

        const val TAG = "ReaderApplication"

        private lateinit var context: Context

        fun appContext() = context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        StrictMode.enableDefaults()
        val start = SystemClock.elapsedRealtime()
        val extensions =
            ModuleApi.getInstance().getExtensions(IApplicationOnCreateListener::class.java)
        LogUtils.d(TAG, "Querying extensions uses ${SystemClock.elapsedRealtime() - start}ms")
        extensions.forEach {
            val methodStart = SystemClock.elapsedRealtime()
            it.onAppCreate(this)
            LogUtils.d(
                TAG,
                "${it.javaClass.name}#onAppCreate costs ${SystemClock.elapsedRealtime() - methodStart}ms"
            )
        }
    }
}
