package com.lyc.easyreader.base.utils.thread

/**
 * Created by Liu Yuchuan on 2020/2/12.
 */
class CancelToken {
    @Volatile
    var canceld = false
        private set

    fun cancel() {
        canceld = true
    }
}
