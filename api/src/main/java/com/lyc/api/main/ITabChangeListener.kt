package com.lyc.api.main

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
interface ITabChangeListener {

    fun onChangeToNewTab(tabId: Int)

    fun onCurrentTabClick(tabId: Int)

}
