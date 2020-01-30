package com.lyc.easyreader.base.route

import android.app.Activity
import com.lyc.appinject.annotations.Extension

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
@Extension
interface IUrlInterceptor {

    fun acceptUrl(url: String): Boolean

    fun handleUrl(urlParams: UrlParams, activityFrom: Activity? = null): Boolean
}
