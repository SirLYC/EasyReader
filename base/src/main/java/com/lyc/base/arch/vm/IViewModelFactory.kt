package com.lyc.base.arch.vm

import androidx.lifecycle.ViewModel
import com.lyc.appinject.annotations.Extension

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
@Extension
interface IViewModelFactory {

    fun <T : ViewModel> createViewMode(modelClass: Class<T>): T?

    // 如果返回true，则下次遇到这个ViewModel会直接查表用这个Factory构建
    // 而不会再遍历Extensions
    fun shouldCache() = false
}
