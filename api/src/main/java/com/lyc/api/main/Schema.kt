package com.lyc.api.main

/**
 * Created by Liu Yuchuan on 2020/1/7.
 */
object Schema {
    const val PROTOCAL_EASY_READER = "lycer://"

    // 主页
    const val MAIN_PAGE = "lycer://main"

    val MAIN_BOOK_SHELF = "lycer://main/${IMainActivityEventBus.ID_BOOK_SHELF}"

    val MAIN_DISCOVER = "lycer://main/${IMainActivityEventBus.ID_DISCOVER}"

    val MAIN_USER_CENTER = "lycer://main/${IMainActivityEventBus.ID_USER_CENTER}"
}
