package com.lyc.easyreader.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.ContextThemeWrapper
import com.lyc.easyreader.base.app.IApplicationOnCreateListener
import com.lyc.easyreader.base.utils.LogUtils
import kotlin.reflect.KClass

/**
 * Created by Liu Yuchuan on 2020/1/7.
 */
class ReaderApplication : Application() {
    companion object {

        private const val TAG = "ReaderApplication"

        private lateinit var context: Context

        fun appContext() =
            context

        val appThemeContext: Context by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ContextThemeWrapper(
                context,
                R.style.AppTheme
            )
        }

        fun openActivity(kClass: KClass<out Activity>) {
            context.startActivity(Intent(context, kClass.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        StrictMode.setVmPolicy(VmPolicy.Builder().detectAll().penaltyLog().build())
        LogUtils.startTiming("Application Querying extensions")
        val extensions =
            getOneToManyApiList<IApplicationOnCreateListener>()
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
