package com.lyc.common.thread

import android.os.Handler
import android.os.Looper
import com.lyc.common.Logger
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

/**
 * Created by Liu Yuchuan on 2020/1/7.
 */


object ExecutorFactory {

    private const val TAG = "ExecutorFactory"
    private fun String.toThreadName(id: Int) = "EX-${this}-${id}"

    private val MAIN_HANDLER: Handler by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(Looper.getMainLooper())
    }

    val CPU_BOUND_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val cpuCount = Runtime.getRuntime().availableProcessors()
        val id = AtomicInteger()
        val resultCount = max(2, cpuCount + 1)

        Logger.globalInstance.d(
            TAG,
            "[CPU_BOUND] cpuCount=$cpuCount, resultCoreThreadCount=$resultCount"
        )

        ThreadPoolExecutor(
            resultCount,
            resultCount,
            60,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            ThreadFactory { Thread(it, "CPU_BOUND".toThreadName(id.incrementAndGet())) },
            RejectedExecutionHandler { r, executor ->
                Logger.globalInstance.e(
                    TAG,
                    "[CPU_BOUND] Rejected, executor=$executor, r=$r"
                )
            })
    }

    val IO_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {

        val cpuCount = Runtime.getRuntime().availableProcessors()
        val id = AtomicInteger()
        val coreCount = max(2, cpuCount + 1)
        val maxCount = max(3, 2 * cpuCount + 1)

        Logger.globalInstance.d(
            TAG,
            "[IO] cpuCount=$cpuCount, coreCount=$coreCount, maxCount=$maxCount"
        )

        ThreadPoolExecutor(
            coreCount,
            maxCount,
            60,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            ThreadFactory { Thread(it, "IO".toThreadName(id.incrementAndGet())) },
            RejectedExecutionHandler { r, executor ->
                Logger.globalInstance.e(
                    TAG,
                    "[IO] Rejected, executor=$executor, r=$r"
                )
            })
    }

    val TIMEOUT_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val id = AtomicInteger()

        ThreadPoolExecutor(
            0,
            Int.MAX_VALUE,
            60,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            ThreadFactory { Thread(it, "TIMEOUT".toThreadName(id.incrementAndGet())) },
            RejectedExecutionHandler { r, executor ->
                Logger.globalInstance.e(
                    TAG,
                    "[TIMEOUT] Rejected, executor=$executor, r=$r"
                )
            })
    }

    val SINGLE_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val id = AtomicInteger()

        ThreadPoolExecutor(
            1,
            1,
            60,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            ThreadFactory { Thread(it, "SINGLE".toThreadName(id.incrementAndGet())) },
            RejectedExecutionHandler { r, executor ->
                Logger.globalInstance.e(
                    TAG,
                    "[SINGLE] Rejected, executor=$executor, r=$r"
                )
            })
    }

    val INSTANT_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Executor {
            it.run()
        }
    }

    val MAIN_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Executor {
            MAIN_HANDLER.post(it)
        }
    }

}
