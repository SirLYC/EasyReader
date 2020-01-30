package com.lyc.easyreader.base.arch.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.BaseFragment

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
inline fun <reified T : ViewModel> BaseActivity.provideViewModel(): T? {
    return ViewModelProvider(
        this,
        ReaderViewModelFactory
    ).get(T::class.java)
}

inline fun <reified T : ViewModel> BaseFragment.provideViewModel(): T? {
    return ViewModelProvider(
        this,
        ReaderViewModelFactory
    ).get(T::class.java)
}
