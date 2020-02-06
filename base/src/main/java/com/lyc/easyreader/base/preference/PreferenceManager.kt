package com.lyc.easyreader.base.preference

import android.content.SharedPreferences
import com.lyc.common.EventHubFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Created by Liu Yuchuan on 2020/2/6.
 */
object PreferenceManager : SharedPreferences.OnSharedPreferenceChangeListener {
    private val preferenceMap = HashMap<String, PreferenceImpl>()
    private val preferenceMapLock = ReentrantLock()
    private val spMap = ConcurrentHashMap<SharedPreferences, PreferenceImpl>()
    private val spMapLock = ReentrantLock()

    private val eventHub = EventHubFactory.createDefault<IPreferenceChangeListener>(true)

    fun getPrefernce(key: String): IPreference {
        return preferenceMapLock.withLock {
            preferenceMap[key] ?: PreferenceImpl(key).also {
                preferenceMap[key] = it
                it.preference.registerOnSharedPreferenceChangeListener(this)
                spMapLock.withLock {
                    spMap[it.preference] = it
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == null) {
            return
        }
        spMap[sharedPreferences]?.let {
            for (eventListener in eventHub.getEventListeners()) {
                if (eventListener.acceptPreference(it)) {
                    eventListener.onPreferenceChanged(it, key)
                }
            }
        }
    }

    fun addPreferenceChangeListener(key: String, listener: IPreferenceChangeListener) {
        eventHub.addEventListener(listener)
    }

    fun removePreferenceChangeListener(prefKey: String, listener: IPreferenceChangeListener) {
        eventHub.removeEventListener(listener)
    }
}
