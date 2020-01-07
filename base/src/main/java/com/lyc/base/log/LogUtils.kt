package com.lyc.base.log

import android.os.Looper
import com.lyc.base.BuildConfig
import com.lyc.base.ReaderApplication
import com.lyc.common.Logger
import com.lyc.common.thread.ExecutorFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Created by Liu Yuchuan on 2020/1/7.
 */
class LogUtils {
    companion object {
        @Volatile
        private var init = false

        private val loggerLock = ReentrantLock()

        private fun initLoggerIfNeeded() {
            if (init) {
                return
            }

            loggerLock.withLock {
                if (init) {
                    return
                }
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    initLogger()
                } else {
                    val latch = CountDownLatch(1)
                    ExecutorFactory.getExecutorByType(ExecutorFactory.MAIN).execute {
                        initLogger()
                        latch.countDown()
                    }
                    latch.await()
                }

                init = true
            }
        }

        private fun initLogger() {
            Logger.singleInstance()
                .init(ReaderApplication.appContext(), BuildConfig.DEBUG, BuildConfig.LOG_FILE)
        }

        fun d(tag: String, msg: String? = null, ex: Throwable? = null) {
            initLoggerIfNeeded()
            Logger.d(tag, msg, ex)
        }

        fun i(tag: String, msg: String? = null, ex: Throwable? = null) {
            initLoggerIfNeeded()
            Logger.i(tag, msg, ex)
        }

        fun w(tag: String, msg: String? = null, ex: Throwable? = null) {
            initLoggerIfNeeded()
            Logger.w(tag, msg, ex)
        }

        fun e(tag: String, msg: String? = null, ex: Throwable? = null) {
            initLoggerIfNeeded()
            Logger.e(tag, msg, ex)
        }

        fun addSpecialNameForTag(tag: String, name: String) {
            Logger.singleInstance().addSpecialTagForFile(tag, name)
        }
    }
}
