package com.lyc.base.route

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
data class UrlParams(
    val url: String,
    val requestCode: Int = -1,
    val contextType: Int = CONTEXT_TYPE_ACTIVITY,
    val intentFlag: Int = 0
) {
    companion object {
        /**
         * 只能从Activity打开
         */
        const val CONTEXT_TYPE_ACTIVITY = 1

        /**
         *
         */
        const val CONTEXT_TYPE_ANY = 5
    }
}
