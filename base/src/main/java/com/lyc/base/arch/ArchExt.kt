package com.lyc.base.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.lyc.base.arch.repo.IRepository
import com.lyc.base.arch.repo.IRepositoryFactory
import com.lyc.base.arch.vm.ReaderViewModelFactory
import com.lyc.base.getAppExtensions
import com.lyc.base.ui.BaseActivity
import com.lyc.base.ui.BaseFragment
import com.lyc.common.Logger

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

inline fun <reified T : IRepository> provideRepository(vararg params: Any): T? {
    for (extension in getAppExtensions<IRepositoryFactory>()) {
        val repo = extension.getRepository(T::class.java, *params)
        if (repo != null) {
            return repo
        }
    }
    Logger.w("ArchExt", "cannot get repository: " + T::class.java.name)
    return null
}
