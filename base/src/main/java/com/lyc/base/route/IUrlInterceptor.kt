package com.lyc.base.route

import android.app.Activity
import com.lyc.appinject.annotations.Extension

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@Extension
interface IUrlInterceptor {

    companion object {
        const val TAG = "IUrlHandler"
    }

    fun acceptUrl(url: String): Boolean

    fun handleUrl(urlParams: UrlParams, activityFrom: Activity? = null): Boolean
}
