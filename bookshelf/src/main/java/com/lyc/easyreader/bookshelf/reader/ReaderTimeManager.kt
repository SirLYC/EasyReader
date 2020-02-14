package com.lyc.easyreader.bookshelf.reader

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.base.preference.PreferenceManager
import com.lyc.easyreader.base.utils.thread.checkMainThread
import java.util.*

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
object ReaderTimeManager : Handler.Callback {

    private const val MSG_TIME_UPDATE = 4

    private val preference = PreferenceManager.getPrefernce("reader_time_manager")
    private var reading = false
    private val handler = Handler(Looper.getMainLooper(), this)
    private var today = System.currentTimeMillis()
    private var lastUpdateTime = System.currentTimeMillis()
    val readTimeTodayLiveData = NonNullLiveData(getReadTimeToday()).apply {
        observeForever {
            preference.putLong(
                "read_time_today",
                it
            )
        }
    }
    val readTimeThisSession = NonNullLiveData(0L)

    private fun getReadTimeToday(): Long {
        val day = preference.getLong("day")
        var needUpdateDay = false
        val time = if (isToday(day)) {
            preference.getLong("read_time_today", 0)
        } else {
            needUpdateDay = true
            0L
        }
        if (needUpdateDay) {
            preference.putLong("day", System.currentTimeMillis())
        }
        return time
    }

    private fun isToday(time: Long): Boolean {
        val today = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        return today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) && today.get(
            Calendar.YEAR
        ) == calendar.get(Calendar.YEAR)
    }

    fun enterRead() {
        checkMainThread()
        if (reading) {
            return
        }
        reading = true
        lastUpdateTime = System.currentTimeMillis()
        readTimeThisSession.value = 0L
        handler.sendEmptyMessage(MSG_TIME_UPDATE)
    }

    fun exitRead() {
        checkMainThread()
        if (!reading) {
            return
        }
        handler.removeCallbacksAndMessages(null)
        reading = false
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_TIME_UPDATE -> {
                if (reading) {
                    val preLastUpdateTime = lastUpdateTime
                    val append = System.currentTimeMillis() - preLastUpdateTime
                    lastUpdateTime = System.currentTimeMillis()
                    if (!isToday(today)) {
                        today = System.currentTimeMillis()
                        preference.putLong("day", today)
                        readTimeTodayLiveData.value = 0
                    } else {
                        readTimeTodayLiveData.value += append
                    }
                    readTimeThisSession.value += append
                    handler.sendEmptyMessageDelayed(MSG_TIME_UPDATE, 60 * 1000L)
                }
            }
        }
        return true
    }
}
