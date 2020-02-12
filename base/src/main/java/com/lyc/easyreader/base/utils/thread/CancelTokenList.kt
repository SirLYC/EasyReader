package com.lyc.easyreader.base.utils.thread

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Liu Yuchuan on 2020/2/12.
 */
class CancelTokenList {
    private val list = CopyOnWriteArrayList<CancelToken>()

    fun add(cancelToken: CancelToken) {
        list.add(cancelToken)
    }

    fun clear() {
        val array = list.toTypedArray()
        list.clear()
        array.forEach {
            it.cancel()
        }
    }
}
