package com.lyc.base.utils

import android.os.Looper
import com.lyc.common.thread.ExecutorFactory
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
        ExecutorFactory.getExecutorByType(ExecutorFactory.MAIN).execute(runnable)
        try {
            latch.await()
        } catch (e: Exception) {
            LogUtils.e(TAG, ex = e)
        }
    }
}
