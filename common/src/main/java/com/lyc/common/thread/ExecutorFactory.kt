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
    /**
     * 以CPU核心数为现状的线程池
     * 适合加解密、复杂算法等
     */
    const val CPU_BOUND = 1

    /**
     * 适合IO操作
     */
    const val IO = 2

    /**
     * 相当于[Thread.start]
     */
    const val TIMEOUT = 3

    /**
     * 单线程，无需同步
     */
    const val SINGLE = 4

    /**
     * 相当于直接调用[Runnable.run]
     */
    const val INSTANT = 5

    /**
     * 主线程
     */
    const val MAIN = 6

    fun getMainExecutor() = MAIN_EXECUTOR
    fun getCpuBoundExecutor() = CPU_BOUND_EXECUTOR
    fun singleExecutor() = SINGLE_EXECUTOR
    fun timeoutExecutor() = TIMEOUT_EXECUTOR
    fun instantExecutor() = INSTANT_EXECUTOR

    fun getExecutorByType(type: Int): Executor = when (type) {
        CPU_BOUND -> CPU_BOUND_EXECUTOR
        IO -> IO_EXECUTOR
        TIMEOUT -> TIMEOUT_EXECUTOR
        SINGLE -> SINGLE_EXECUTOR
        INSTANT -> INSTANT_EXECUTOR
        MAIN -> MAIN_EXECUTOR
        else -> {
            Logger.w(
                TAG,
                "Invalid executor type! Type=$type"
            )
            INSTANT_EXECUTOR
        }
    }


    private const val TAG = "ExecutorFactory"
    private fun String.toThreadName(id: Int) = "EX-${this}-${id}"

    private val MAIN_HANDLER: Handler by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(Looper.getMainLooper())
    }

    private val CPU_BOUND_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val cpuCount = Runtime.getRuntime().availableProcessors()
        val id = AtomicInteger()
        val resultCount = max(2, cpuCount + 1)

        Logger.d(
            TAG,
            "[CPU_BOUND] cpuCount=$cpuCount, resultCoreThreadCount=$resultCount"
        )

        ThreadPoolExecutor(
            resultCount,
            resultCount,
            60,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            ThreadFactory { Thread("CPU_BOUND".toThreadName(id.incrementAndGet())) },
            RejectedExecutionHandler { r, executor ->
                Logger.e(
                    TAG,
                    "[CPU_BOUND] Rejected, executor=$executor, r=$r"
                )
            })
    }

    private val IO_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {

        val cpuCount = Runtime.getRuntime().availableProcessors()
        val id = AtomicInteger()
        val coreCount = max(2, cpuCount + 1)
        val maxCount = max(3, 2 * cpuCount + 1)

        Logger.d(
            TAG,
            "[IO] cpuCount=$cpuCount, coreCount=$coreCount, maxCount=$maxCount"
        )

        ThreadPoolExecutor(
            coreCount,
            maxCount,
            60,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            ThreadFactory { Thread("IO".toThreadName(id.incrementAndGet())) },
            RejectedExecutionHandler { r, executor ->
                Logger.e(
                    TAG,
                    "[IO] Rejected, executor=$executor, r=$r"
                )
            })
    }

    private val TIMEOUT_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val id = AtomicInteger()

        ThreadPoolExecutor(
            0,
            Int.MAX_VALUE,
            60,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            ThreadFactory { Thread("TIMEOUT".toThreadName(id.incrementAndGet())) },
            RejectedExecutionHandler { r, executor ->
                Logger.e(
                    TAG,
                    "[TIMEOUT] Rejected, executor=$executor, r=$r"
                )
            })
    }

    private val SINGLE_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val id = AtomicInteger()

        ThreadPoolExecutor(
            1,
            1,
            60,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            ThreadFactory { Thread("SINGLE".toThreadName(id.incrementAndGet())) },
            RejectedExecutionHandler { r, executor ->
                Logger.e(
                    TAG,
                    "[SINGLE] Rejected, executor=$executor, r=$r"
                )
            })
    }

    private val INSTANT_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Executor {
            it.run()
        }
    }

    private val MAIN_EXECUTOR: Executor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Executor {
            MAIN_HANDLER.post(it)
        }
    }

}
