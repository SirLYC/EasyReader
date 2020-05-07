package com.lyc.easyreader.base.app

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.lyc.appinject.annotations.InjectApiImpl
import com.lyc.easyreader.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/5/7.
 */
@InjectApiImpl(api = IApplicationOnCreateListener::class)
class MainThreadBlockDetector : IApplicationOnCreateListener, Handler.Callback {
    private companion object {
        private const val START = ">>>>> Dispatching"
        private const val END = "<<<<< Finished"

        private const val TIME_DELAY = 100L

        private const val MSG_TIMEOUT = 1

        private const val TAG = "MainThreadBlock"
    }

    @Volatile
    private var mainThreadHandlingMsg = false

    private val logThread = HandlerThread("MainThreadBlockDetector")

    private val handler: Handler

    init {
        logThread.start()
        handler = Handler(logThread.looper, this)
    }

    override fun onAppCreate(application: Application) {
        application.mainLooper.setMessageLogging { msg ->
            if (msg.startsWith(START)) {
                mainThreadHandlingMsg = true
                handler.sendEmptyMessageDelayed(MSG_TIMEOUT, TIME_DELAY)
            } else if (msg.startsWith(END)) {
                mainThreadHandlingMsg = false
                handler.removeMessages(MSG_TIMEOUT)
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == MSG_TIMEOUT && mainThreadHandlingMsg) {
            val sb = StringBuilder()
            val stackTrace =
                Looper.getMainLooper().thread.stackTrace
            for (s in stackTrace) {
                sb.append(s.toString())
                sb.append("\n")
            }
            LogUtils.w(TAG, sb.toString())
            return true
        }

        return false
    }
}
