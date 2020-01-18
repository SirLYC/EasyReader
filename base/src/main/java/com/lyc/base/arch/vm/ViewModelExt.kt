package com.lyc.base.arch.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.lyc.base.ui.BaseActivity
import com.lyc.base.ui.BaseFragment

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
inline fun <reified T : ViewModel> BaseActivity.provideViewModel(): T? {
    return ViewModelProviders.of(
        this,
        ReaderViewModelFactory
    ).get(T::class.java)
}

inline fun <reified T : ViewModel> BaseFragment.provideViewModel(): T? {
    return ViewModelProviders.of(
        this,
        ReaderViewModelFactory
    ).get(T::class.java)
}
