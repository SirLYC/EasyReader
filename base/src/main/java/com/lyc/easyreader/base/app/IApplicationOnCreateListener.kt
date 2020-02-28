package com.lyc.easyreader.base.app

import android.app.Application
import com.lyc.appinject.annotations.InjectApi

/**
 * Created by Liu Yuchuan on 2020/1/16.
 */
@InjectApi(oneToMany = true)
interface IApplicationOnCreateListener {
    fun onAppCreate(application: Application)
}
