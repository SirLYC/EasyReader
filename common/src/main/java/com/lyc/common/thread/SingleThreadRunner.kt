package com.lyc.common.thread

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.lyc.common.Logger
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write

/**
 * Created by Liu Yuchuan on 2020/1/5.
 */
class SingleThreadRunner @JvmOverloads constructor(
    private val name: String,
    private var callback: Handler.Callback? = null
) {

    companion object {
        private const val TAG = "SingleThreadRunner"
    }

    private val thread = HandlerThread(name)

    private val stateLock = ReentrantReadWriteLock()

    @Volatile
    private var handler: Handler? = null
    @Volatile
    private var started = false
    @Volatile
    private var stopped = false

    fun getHandler(): Handler? {
        startIfNeeded()
        return handler
    }

    /**
     * @return false when this runner is stopped
     */
    private inline fun runWithHandler(action: (handler: Handler) -> Unit): Boolean {
        var result = false
        startIfNeeded()
        stateLock.read {
            val handler = this.handler
            if (handler != null) {
                action(handler)
                result = true
            } else {
                Logger.w(
                    TAG,
                    "[${name}] No handler present! started=${started}, stopped=${stopped}"
                )
            }
        }
        return result
    }

    fun await(): Boolean {
        if (Looper.myLooper() == thread.looper) {
            return false
        }

        return runWithHandler {
            val lock = ReentrantLock()
            val condition = lock.newCondition()
            it.post {
                lock.withLock {
                    condition.signal()
                }
            }
            lock.withLock {
                try {
                    condition.await()
                } catch (e: InterruptedException) {
                    Logger.e(
                        TAG,
                        "[${name}]",
                        e
                    )
                }
            }
        }
    }

    @JvmOverloads
    fun awaitRun(runnable: Runnable, forceAsync: Boolean = true): Boolean {
        if ((forceAsync || Looper.myLooper() == Looper.getMainLooper()) && Looper.myLooper() != thread.looper) {
            return runWithHandler {
                val lock = ReentrantLock()
                val condition = lock.newCondition()
                it.post {
                    runnable.run()
                    lock.withLock {
                        condition.signal()
                    }
                }
                lock.withLock {
                    try {
                        condition.await()
                    } catch (e: InterruptedException) {
                        Logger.e(
                            TAG,
                            "[${name}]",
                            e
                        )
                    }
                }
            }
        }

        runnable.run()
        return true
    }

    @JvmOverloads
    fun asyncRun(runnable: Runnable, forceAsync: Boolean = true): Boolean {
        if ((forceAsync || Looper.myLooper() == Looper.getMainLooper())) {
            return runWithHandler {
                it.post {
                    runnable.run()
                }
            }
        }

        runnable.run()
        return true
    }

    private fun startIfNeeded() {
        if (started || stopped) {
            return
        }

        stateLock.write {
            if (started || stopped) {
                return
            }

            thread.start()
            handler = Handler(thread.looper, callback)
        }
    }

    fun stop() {
        if (stopped) {
            return
        }

        stateLock.write {
            if (stopped) {
                return
            }

            stopped = true
            thread.quit()
            handler = null
        }
    }
}
