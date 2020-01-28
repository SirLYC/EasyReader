package com.lyc.bookshelf

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.lyc.base.arch.NonNullLiveData
import com.lyc.base.utils.rv.ObservableList

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class BookShelfViewModel : ViewModel() {
    private val handler = Handler(Looper.getMainLooper())

    val loadStatusLivaData = NonNullLiveData<Boolean>(false)
    val list = ObservableList(arrayListOf<Pair<Int, Any>>())

    fun refreshList() {

    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }
}
