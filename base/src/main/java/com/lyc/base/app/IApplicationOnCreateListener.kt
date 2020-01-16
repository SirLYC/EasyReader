package com.lyc.base.app

import android.app.Application
import com.lyc.appinject.annotations.Extension

/**
 * Created by Liu Yuchuan on 2020/1/16.
 */
@Extension
interface IApplicationOnCreateListener {
    fun onAppCreate(application: Application)
}
