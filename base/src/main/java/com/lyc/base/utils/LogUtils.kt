package com.lyc.base.utils

import android.os.SystemClock
import com.lyc.base.BuildConfig
import com.lyc.base.ReaderApplication
import com.lyc.common.Logger
import java.util.concurrent.ConcurrentHashMap
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

        private val timingMap by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { ConcurrentHashMap<String, Long>() }

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
                logFileName = "Log-${BuildConfig.VERSION_CODE}.${BuildConfig.VERSION_NAME}"
                outputToConsole = BuildConfig.LOG_CONSOLE
                outputToFile = BuildConfig.LOG_FILE
                init(ReaderApplication.appContext())
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

        fun startTiming(key: String) {
            timingMap[key] = SystemClock.elapsedRealtime()
        }

        fun debugLogTiming(
            tag: String,
            msg: String? = null,
            key: String,
            ex: Throwable? = null
        ) {
            logTiming(tag, msg, key, Level.DEBUG, ex)
        }

        fun infoLogTiming(
            tag: String,
            msg: String? = null,
            key: String,
            ex: Throwable? = null
        ) {
            logTiming(tag, msg, key, Level.INFO, ex)
        }

        fun warnLogTiming(
            tag: String,
            msg: String? = null,
            key: String,
            ex: Throwable? = null
        ) {
            logTiming(tag, msg, key, Level.WARN, ex)
        }

        fun errorLogTiming(
            tag: String,
            msg: String? = null,
            key: String,
            ex: Throwable? = null
        ) {
            logTiming(tag, msg, key, Level.ERROR, ex)
        }

        private fun logTiming(
            tag: String,
            msg: String? = null,
            key: String,
            level: Level,
            ex: Throwable? = null
        ) {
            val current = SystemClock.elapsedRealtime()
            val lastTime = timingMap.remove(key) ?: current - 1
            val logMsg = "[${current - lastTime}ms] ${msg ?: ""}"
            when (level) {
                Level.INFO -> {
                    i(tag, logMsg, ex)
                }

                Level.WARN -> {
                    w(tag, logMsg, ex)
                }

                Level.ERROR -> {
                    e(tag, logMsg, ex)
                }

                Level.DEBUG -> {
                    d(tag, logMsg, ex)
                }
            }
        }
    }

    enum class Level {
        DEBUG, INFO, WARN, ERROR
    }
}
