package com.lyc.common

import java.lang.ref.WeakReference
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Created by Liu Yuchuan on 2020/1/5.
 */
interface EventHub<T> {

    /**
     * Add a listener to the hub
     */
    fun addEventListener(listener: T?)

    /**
     * Remove the listener from the hub
     */
    fun removeEventListener(listener: T?)

    /**
     * Get all listeners that non-null and present in the hub
     * @return listener list
     */
    fun getEventListeners(): List<T>

    /**
     * Iterate all listeners that non-null and present in the hub
     * Based on method [getEventListeners]
     * [action]: actions with every listener as arg
     */
    fun forEachListener(action: (listener: T) -> Unit)
}

data class EventHubParam @JvmOverloads constructor(
    val threadSafe: Boolean = true,
    val repeatable: Boolean = false,
    val weakRef: Boolean = false
)

class EventHubFactory private constructor() {
    companion object {
        @JvmStatic
        fun <T> createDefault(threadSafe: Boolean): EventHub<T> {
            return NonRepeatableEventHub(threadSafe)
        }

        @JvmStatic
        fun <T> createEventHub(param: EventHubParam): EventHub<T> {
            if (param.weakRef && param.repeatable) {
                return RepeatableWeakEventHub(param.threadSafe)
            }

            if (param.weakRef && !param.repeatable) {
                return NonRepeatableWeakEventHub(param.threadSafe)
            }

            if (param.repeatable) {
                return RepeatableEventHub(param.threadSafe)
            }

            return createDefault(param.threadSafe)
        }
    }
}

private abstract class AbstractEventHub<T>(private val threadSafe: Boolean) : EventHub<T> {

    private val readWriteLock = ReentrantReadWriteLock()

    protected inline fun read(func: () -> Unit) {
        if (threadSafe) {
            readWriteLock.read(func)
        } else {
            func()
        }
    }

    protected inline fun write(func: () -> Unit) {
        if (threadSafe) {
            readWriteLock.write(func)
        } else {
            func()
        }
    }

    override fun forEachListener(action: (listener: T) -> Unit) {
        getEventListeners().forEach {
            action(it)
        }
    }
}

private class RepeatableEventHub<T>(threadSafe: Boolean) : AbstractEventHub<T>(threadSafe) {

    private val listeners = arrayListOf<T>()

    override fun addEventListener(listener: T?) {
        if (listener == null) {
            return
        }
        write {
            listeners.add(listener)
        }
    }

    override fun removeEventListener(listener: T?) {
        if (listener == null) {
            return
        }
        write {
            listeners.remove(listener)
        }
    }

    override fun getEventListeners(): List<T> {
        val result = arrayListOf<T>()
        read {
            result.addAll(listeners)
        }
        return result
    }
}

private class NonRepeatableEventHub<T>(threadSafe: Boolean) : AbstractEventHub<T>(threadSafe) {

    private val listeners = LinkedHashSet<T>()

    override fun addEventListener(listener: T?) {
        if (listener == null) {
            return
        }
        write {
            listeners.add(listener)
        }
    }

    override fun removeEventListener(listener: T?) {
        if (listener == null) {
            return
        }
        write {
            listeners.remove(listener)
        }
    }

    override fun getEventListeners(): List<T> {
        val result = arrayListOf<T>()
        read {
            result.addAll(listeners)
        }
        return result
    }
}

private class RepeatableWeakEventHub<T>(threadSafe: Boolean) : AbstractEventHub<T>(threadSafe) {

    private val listeners = arrayListOf<WeakReference<T>>()

    override fun addEventListener(listener: T?) {
        if (listener == null) {
            return
        }
        write {
            listeners.add(WeakReference(listener))
        }
    }

    override fun removeEventListener(listener: T?) {
        if (listener == null) {
            return
        }
        write {
            listeners.iterator().let {
                while (it.hasNext()) {
                    val next = it.next()
                    val value = next.get()
                    if (value == null) {
                        it.remove()
                    } else if (value == listener) {
                        break
                    }
                }
            }
        }
    }

    override fun getEventListeners(): List<T> {
        val result = arrayListOf<T>()
        read {
            listeners.forEach {
                it.get()?.let(result::add)
            }
        }
        return result
    }
}

private class NonRepeatableWeakEventHub<T>(threadSafe: Boolean) : AbstractEventHub<T>(threadSafe) {

    private val listeners = arrayListOf<WeakReference<T>>()

    override fun addEventListener(listener: T?) {
        if (listener == null) {
            return
        }
        write {
            listeners.iterator().let {
                var added = false
                while (it.hasNext()) {
                    val next = it.next()
                    val value = next.get()
                    if (value == null) {
                        it.remove()
                    } else if (value == listener) {
                        added = true
                        break
                    }
                }

                if (!added) {
                    listeners.add(WeakReference(listener))
                }
            }
        }
    }

    override fun removeEventListener(listener: T?) {
        if (listener == null) {
            return
        }
        write {
            listeners.iterator().let {
                while (it.hasNext()) {
                    val next = it.next()
                    val value = next.get()
                    if (value == null) {
                        it.remove()
                    } else if (value == listener) {
                        break
                    }
                }
            }
        }
    }

    override fun getEventListeners(): List<T> {
        val result = arrayListOf<T>()
        read {
            listeners.forEach {
                it.get()?.let(result::add)
            }
        }
        return result
    }
}
