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
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.getOrSet

/**
 * Created by Liu Yuchuan on 2020/1/5.
 */
class Logger : Handler.Callback {
    companion object {

        private const val MSG_ADD_PENDING = 1
        private const val MSG_WRITE_FILE = 2

        private val timeFormatThreadLocal = ThreadLocal<SimpleDateFormat>()

        @JvmStatic
        val globalInstance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Logger()
        }

        fun toStackTraceString(tr: Throwable?): String? {
            if (tr == null) {
                return ""
            }
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            tr.printStackTrace(pw)
            pw.flush()
            return sw.toString()
        }
    }

    private constructor()

    constructor(context: Context, logFileName: String) {
        this.appContext = context.applicationContext
        this.logFileName = logFileName
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
        context: Context
    ) {
        appContext = context.applicationContext
    }

    fun addSpecialTagForFile(logTag: String, name: String) {
        globalInstance.specialTagMap[logTag] = name
    }

    @JvmOverloads
    fun d(
        tag: String,
        msg: String? = null,
        ex: Throwable? = null,
        outputToConsole: Boolean = globalInstance.outputToConsole,
        outputToFile: Boolean = globalInstance.outputToFile
    ) {
        if (outputToConsole) {
            Log.d(tag, msg, ex)
        }

        if (outputToFile) {
            outputToFile(Level.DEBUG, tag, msg, ex)
        }
    }

    @JvmOverloads
    fun i(
        tag: String, msg: String? = null, ex: Throwable? = null,
        outputToConsole: Boolean = globalInstance.outputToConsole,
        outputToFile: Boolean = globalInstance.outputToFile
    ) {
        if (outputToConsole) {
            Log.i(tag, msg, ex)
        }

        if (outputToFile) {
            outputToFile(Level.INFO, tag, msg, ex)
        }
    }

    @JvmOverloads
    fun w(
        tag: String,
        msg: String? = null,
        ex: Throwable? = null,
        outputToConsole: Boolean = globalInstance.outputToConsole,
        outputToFile: Boolean = globalInstance.outputToFile
    ) {
        if (outputToConsole) {
            Log.w(tag, msg, ex)
        }

        if (outputToFile) {
            outputToFile(Level.WARN, tag, msg, ex)
        }
    }

    @JvmOverloads
    fun e(
        tag: String,
        msg: String? = null,
        ex: Throwable? = null,
        outputToConsole: Boolean = globalInstance.outputToConsole,
        outputToFile: Boolean = globalInstance.outputToFile
    ) {
        if (outputToConsole) {
            Log.e(tag, msg, ex)
        }

        if (outputToFile) {
            outputToFile(Level.ERROR, tag, msg, ex)
        }
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

    private fun outputToFile(level: Level, tag: String, msg: String?, ex: Throwable?) {
        globalInstance.fileRunner.getHandler()?.obtainMessage(
            MSG_ADD_PENDING,
            LogEntry(level, tag, msg, ex, System.currentTimeMillis())
        )?.sendToTarget()
    }

    fun waitForWriteFinish() {
        fileRunner.await()
    }

    private class LogEntry(
        val level: Level,
        val tag: String,
        val msg: String?,
        val ex: Throwable?,
        val time: Long
    ) {
        override fun toString(): String {
            return "$level|$tag|${msg.let {
                it ?: if (ex != null) ""
                else it
            }}${ex.let {
                if (it == null) {
                    ""
                } else {
                    "\n${toStackTraceString(it)}"
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
                    it.sendEmptyMessage(MSG_WRITE_FILE)
                }
            }

            MSG_WRITE_FILE -> {
                doWriteFile()
            }
        }

        return true
    }

    enum class Level {
        DEBUG, INFO, WARN, ERROR
    }
}
