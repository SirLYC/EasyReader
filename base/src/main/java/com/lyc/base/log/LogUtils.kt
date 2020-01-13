package com.lyc.base.log

import com.lyc.base.BuildConfig
import com.lyc.base.ReaderApplication
import com.lyc.base.waitFinishOnMain
import com.lyc.common.Logger
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
                waitFinishOnMain(Runnable {
                    initLogger()
                })

                init = true
            }
        }

        private fun initLogger() {
            Logger.instance.apply {
                init(ReaderApplication.appContext())
                outputToConsole = BuildConfig.LOG_CONSOLE
                outputToFile = BuildConfig.LOG_FILE
            }
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
            initLoggerIfNeeded()
            Logger.instance.addSpecialTagForFile(tag, name)
        }
    }
}
