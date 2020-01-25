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
            Logger.globalInstance.apply {
                logFileName = "Log-${BuildConfig.VERSION_CODE}.${BuildConfig.VERSION_NAME}"
                outputToConsole = BuildConfig.LOG_CONSOLE
                outputToFile = BuildConfig.LOG_FILE
                init(ReaderApplication.appContext())
            }
        }

        fun d(
            tag: String,
            msg: String? = null,
            ex: Throwable? = null,
            outputToConsole: Boolean = Logger.globalInstance.outputToConsole,
            outputToFile: Boolean = Logger.globalInstance.outputToFile
        ) {
            initLoggerIfNeeded()
            Logger.globalInstance.d(tag, msg, ex, outputToConsole, outputToFile)
        }

        fun i(
            tag: String, msg: String? = null, ex: Throwable? = null,
            outputToConsole: Boolean = Logger.globalInstance.outputToConsole,
            outputToFile: Boolean = Logger.globalInstance.outputToFile
        ) {
            initLoggerIfNeeded()
            Logger.globalInstance.i(tag, msg, ex, outputToConsole, outputToFile)
        }

        fun w(
            tag: String, msg: String? = null, ex: Throwable? = null,
            outputToConsole: Boolean = Logger.globalInstance.outputToConsole,
            outputToFile: Boolean = Logger.globalInstance.outputToFile
        ) {
            initLoggerIfNeeded()
            Logger.globalInstance.w(tag, msg, ex, outputToConsole, outputToFile)
        }

        fun e(
            tag: String, msg: String? = null, ex: Throwable? = null,
            outputToConsole: Boolean = Logger.globalInstance.outputToConsole,
            outputToFile: Boolean = Logger.globalInstance.outputToFile
        ) {
            initLoggerIfNeeded()
            Logger.globalInstance.e(tag, msg, ex, outputToConsole, outputToFile)
        }

        fun addSpecialNameForTag(tag: String, name: String) {
            initLoggerIfNeeded()
            Logger.globalInstance.addSpecialTagForFile(tag, name)
        }

        fun startTiming(key: String) {
            timingMap[key] = SystemClock.elapsedRealtime()
        }

        fun debugLogTiming(
            tag: String,
            msg: String? = null,
            key: String,
            ex: Throwable? = null,
            outputToConsole: Boolean = Logger.globalInstance.outputToConsole,
            outputToFile: Boolean = Logger.globalInstance.outputToFile
        ) {
            logTiming(tag, msg, key, Level.DEBUG, ex, outputToConsole, outputToFile)
        }

        fun infoLogTiming(
            tag: String,
            msg: String? = null,
            key: String,
            ex: Throwable? = null,
            outputToConsole: Boolean = Logger.globalInstance.outputToConsole,
            outputToFile: Boolean = Logger.globalInstance.outputToFile
        ) {
            logTiming(tag, msg, key, Level.INFO, ex, outputToConsole, outputToFile)
        }

        fun warnLogTiming(
            tag: String,
            msg: String? = null,
            key: String,
            ex: Throwable? = null,
            outputToConsole: Boolean = Logger.globalInstance.outputToConsole,
            outputToFile: Boolean = Logger.globalInstance.outputToFile
        ) {
            logTiming(tag, msg, key, Level.WARN, ex, outputToConsole, outputToFile)
        }

        fun errorLogTiming(
            tag: String,
            msg: String? = null,
            key: String,
            ex: Throwable? = null,
            outputToConsole: Boolean = Logger.globalInstance.outputToConsole,
            outputToFile: Boolean = Logger.globalInstance.outputToFile
        ) {
            logTiming(tag, msg, key, Level.ERROR, ex, outputToConsole, outputToFile)
        }

        private fun logTiming(
            tag: String,
            msg: String? = null,
            key: String,
            level: Level,
            ex: Throwable? = null,
            outputToConsole: Boolean,
            outputToFile: Boolean
        ) {
            val current = SystemClock.elapsedRealtime()
            val lastTime = timingMap.remove(key) ?: current - 1
            val logMsg = "[${current - lastTime}ms] ${msg ?: ""}"
            when (level) {
                Level.INFO -> {
                    i(tag, logMsg, ex, outputToConsole, outputToFile)
                }

                Level.WARN -> {
                    w(tag, logMsg, ex, outputToConsole, outputToFile)
                }

                Level.ERROR -> {
                    e(tag, logMsg, ex, outputToConsole, outputToFile)
                }

                Level.DEBUG -> {
                    d(tag, logMsg, ex, outputToConsole, outputToFile)
                }
            }
        }
    }

    enum class Level {
        DEBUG, INFO, WARN, ERROR
    }
}
