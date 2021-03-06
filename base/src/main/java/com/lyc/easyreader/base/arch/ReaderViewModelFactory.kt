package com.lyc.easyreader.base.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyc.easyreader.base.getOneToManyApiList
import com.lyc.easyreader.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
object ReaderViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    private const val TAG = "ReaderViewModelFactory"
    private val createFactoryMap = hashMapOf<Class<out ViewModel>, IViewModelFactory>()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val factory = createFactoryMap[modelClass]
        if (factory != null) {
            return factory.createViewMode(modelClass)!!
        }

        val extensions =
            getOneToManyApiList<IViewModelFactory>()
        for (extension in extensions) {
            val viewModel = extension.createViewMode(modelClass)
            if (viewModel != null) {
                if (extension.shouldCache()) {
                    createFactoryMap[modelClass] = extension
                }
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
