package com.lyc.base.arch.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyc.base.getAppExtensions
import com.lyc.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
object ReaderViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    private const val TAG = "ReaderViewModelFactory"

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val extensions = getAppExtensions<IViewModelFactory>()
        for (extension in extensions) {
            val viewModel = extension.create(modelClass)
            if (viewModel != null) {
                return viewModel
            }
        }

        LogUtils.d(
            TAG,
            "Cannot create $modelClass by IViewModelFactory extensions! Use default new instance!"
        )

        return super.create(modelClass)
    }
}
