package com.lyc.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import com.lyc.common.thread.SingleThreadRunner
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.getOrSet

/**
 * Created by Liu Yuchuan on 2020/1/5.
 */
class Logger private constructor() : Handler.Callback {
    companion object {

        private const val MSG_ADD_PENDING = 1
        private const val MSG_WRITE_FILE = 2

        private const val WRITE_DELAY = 1000L

        private const val LEVEL_DEBUG = 1
        private const val LEVEL_INFO = 2
        private const val LEVEL_WARNING = 3
        private const val LEVEL_ERROR = 4

        private val timeFormatThreadLocal = ThreadLocal<SimpleDateFormat>()

        private val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Logger()
        }

        fun singleInstance() = instance

        private fun Int.level2String() = when (this) {
            LEVEL_DEBUG -> "DEBUG"
            LEVEL_INFO -> "INFO"
            LEVEL_WARNING -> "WARNING"
            LEVEL_ERROR -> "ERROR"
            else -> "UNKNOWN"
        }

        private fun outputToFile(level: Int, tag: String, msg: String?, ex: Throwable?) {
            instance.fileRunner.getHandler()?.obtainMessage(
                MSG_ADD_PENDING,
                LogEntry(level, tag, msg, ex, System.currentTimeMillis())
            )?.sendToTarget()
        }

        // ----------------------------------------- API ----------------------------------------- //
        // 看起来都是静态方法，实际上都是通过单例去操作的

        @JvmOverloads
        @JvmStatic
        fun d(tag: String, msg: String? = null, ex: Throwable? = null) {
            if (instance.outputToConsole) {
                Log.d(tag, msg, ex)
            }

            if (instance.outputToFile) {
                outputToFile(LEVEL_DEBUG, tag, msg, ex)
            }
        }

        @JvmOverloads
        @JvmStatic
        fun i(tag: String, msg: String? = null, ex: Throwable? = null) {
            if (instance.outputToConsole) {
                Log.i(tag, msg, ex)
            }

            if (instance.outputToFile) {
                outputToFile(LEVEL_INFO, tag, msg, ex)
            }
        }

        @JvmOverloads
        @JvmStatic
        fun w(tag: String, msg: String? = null, ex: Throwable? = null) {
            if (instance.outputToConsole) {
                Log.w(tag, msg, ex)
            }

            if (instance.outputToFile) {
                outputToFile(LEVEL_WARNING, tag, msg, ex)
            }
        }

        @JvmOverloads
        @JvmStatic
        fun e(tag: String, msg: String? = null, ex: Throwable? = null) {
            if (instance.outputToConsole) {
                Log.e(tag, msg, ex)
            }

            if (instance.outputToFile) {
                outputToFile(LEVEL_ERROR, tag, msg, ex)
            }
        }
    }

    private var appContext: Context? = null

    @Volatile
    var outputToConsole = true
    @Volatile
    var outputToFile = true

    private val fileRunner =
        SingleThreadRunner("LYC-Logs", instance)
    private val pendingLogEntry = CopyOnWriteArrayList<LogEntry>()

    fun init(context: Context, outputToConsole: Boolean = true, outputToFile: Boolean = true) {
        appContext = context.applicationContext
        this.outputToFile = outputToFile
        this.outputToConsole = outputToConsole
    }

    @SuppressLint("SimpleDateFormat")
    private fun doWriteFile() {
        appContext?.getExternalFilesDir(".Logs")?.let { dirPath ->
            File(dirPath, "lyc-log").printWriter().let { writer ->
                val list = ArrayList(pendingLogEntry)
                val format = timeFormatThreadLocal.getOrSet {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S ")
                }
                for (logEntry in list) {
                    writer.println("${format.format(logEntry.time)}|${logEntry}")
                }
            }
        }
    }

    private class LogEntry(
        val level: Int,
        val tag: String,
        val msg: String?,
        val ex: Throwable?,
        val time: Long
    ) {
        override fun toString(): String {
            return "${level.level2String()}|$tag|${msg}${ex.let {
                if (it == null) {
                    ""
                } else {
                    "\n${Log.getStackTraceString(it)}"
                }
            }
            }"
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_ADD_PENDING -> {
                (msg.obj as? LogEntry)?.let {
                    pendingLogEntry.add(it)
                }
                fileRunner.getHandler()?.let {
                    it.removeMessages(MSG_WRITE_FILE)
                    it.sendEmptyMessageDelayed(MSG_WRITE_FILE, WRITE_DELAY)
                }
            }

            MSG_WRITE_FILE -> {
                doWriteFile()
            }
        }

        return true
    }
}
