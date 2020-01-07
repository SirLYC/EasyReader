package com.lyc.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import com.lyc.common.thread.SingleThreadRunner
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentHashMap
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
    @Volatile
    var logFileName: String = "logger"
        set(value) {
            if (value.isNotBlank()) {
                field = value
            }
        }

    private val fileRunner =
        SingleThreadRunner("Logger", this)
    private val pendingLogEntry = CopyOnWriteArrayList<LogEntry>()
    private val specialTagMap = ConcurrentHashMap<String, String>()

    fun init(
        context: Context,
        outputToConsole: Boolean = true,
        outputToFile: Boolean = true,
        logFileName: String = ""
    ) {
        appContext = context.applicationContext
        this.outputToFile = outputToFile
        this.outputToConsole = outputToConsole
        if (logFileName.isNotBlank()) {
            this.logFileName = logFileName
        }
    }

    fun addSpecialTagForFile(logTag: String, name: String) {
        instance.specialTagMap[logTag] = name
    }

    @SuppressLint("SimpleDateFormat")
    private fun doWriteFile() {
        val specialMapSnapshot = HashMap(specialTagMap)
        val format = timeFormatThreadLocal.getOrSet {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S ")
        }
        val specialLogs = HashMap<String, MutableList<LogEntry>>()

        val logEntriesToRemove = mutableListOf<LogEntry>()

        appendToLogFile("${logFileName}.log") { writer ->
            val list = ArrayList(pendingLogEntry)
            for (logEntry in list) {
                val specialName = specialMapSnapshot[logEntry.tag]
                if (specialName?.isNotEmpty() == true) {
                    specialLogs.getOrPut(specialName, { arrayListOf() }).add(logEntry)
                } else {
                    writer.println("${format.format(logEntry.time)}|${logEntry}")
                    logEntriesToRemove.add(logEntry)
                }
            }
        }

        specialLogs.forEach { entry ->
            appendToLogFile("${logFileName}-${entry.key}.log") { writer ->
                entry.value.forEach { logEntry ->
                    writer.println("${format.format(logEntry.time)}|${logEntry}")
                    logEntriesToRemove.add(logEntry)
                }
            }
        }

        pendingLogEntry.removeAll(logEntriesToRemove)
    }

    private inline fun appendToLogFile(filename: String, action: (writer: PrintWriter) -> Unit) {
        try {
            appContext?.getExternalFilesDir(".logger")?.let { dirPath ->
                if ((dirPath.exists() && dirPath.isDirectory) || dirPath.mkdirs()) {
                    PrintWriter(FileOutputStream(File(dirPath, filename), true)).use {
                        action(it)
                        it.flush()
                    }
                }
            }
        } catch (t: Throwable) {
            d("Logger", ex = t)
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
