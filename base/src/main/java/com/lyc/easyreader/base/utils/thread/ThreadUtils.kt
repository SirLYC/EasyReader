package com.lyc.easyreader.base.utils.thread

import android.os.Looper
import com.lyc.common.thread.ExecutorFactory
import com.lyc.easyreader.base.utils.LogUtils
import java.util.concurrent.CountDownLatch

/**
 * Created by Liu Yuchuan on 2020/1/8.
 */
private const val TAG = "ThreadUtils"

fun waitFinishOnMain(runnable: Runnable) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        runnable.run()
    } else {
        val latch = CountDownLatch(1)
        ExecutorFactory.MAIN_EXECUTOR.execute(runnable)
        try {
            latch.await()
        } catch (e: Exception) {
            LogUtils.e(
                TAG,
                ex = e
            )
        }
    }
}

fun doOnMainThread(runnable: Runnable) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        runnable.run()
    } else {
        ExecutorFactory.MAIN_EXECUTOR.execute(runnable)
    }
}


fun checkMainThread() {
    if (Looper.getMainLooper() != Looper.myLooper()) {
        throw IllegalStateException("This method is only allowed run on main thread! Current thread=${Thread.currentThread()}.")
    }
}
