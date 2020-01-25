package com.lyc.base.route

import android.content.Intent
import android.net.Uri
import com.lyc.base.ReaderApplication
import com.lyc.base.app.ActivityCollector
import com.lyc.base.getAppExtensions
import com.lyc.base.ui.BaseActivity
import com.lyc.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
object AppRoute {

    const val TAG = "AppRoute"

    fun jumpToUrl(urlParams: UrlParams, activityFrom: BaseActivity? = null) {
        LogUtils.i(TAG, "start jumpToUrl: $urlParams")
        for (appExtension in getAppExtensions<IUrlInterceptor>()) {
            if (appExtension.acceptUrl(urlParams.url) and appExtension.handleUrl(urlParams)) {
                LogUtils.i(TAG, "$urlParams intercepted by ${appExtension.javaClass.name}")
                return
            }
        }

        LogUtils.i(TAG, "$urlParams cannot be handled by extension, use default.")

        val intent = Intent()
        intent.data = Uri.parse(urlParams.url)
        intent.action = Intent.ACTION_VIEW
        if (urlParams.contextType == UrlParams.CONTEXT_TYPE_ANY && urlParams.requestCode <= 0 && activityFrom == null) {
            intent.flags = urlParams.intentFlag or Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                ReaderApplication.appContext().startActivity(intent)
                LogUtils.i(TAG, "$urlParams open activity from application context.")
                return
            } catch (e: Exception) {
                LogUtils.e(IUrlInterceptor.TAG, "Cannot open activity for $urlParams", e)
            }
        }

        intent.flags = urlParams.intentFlag

        (activityFrom ?: ActivityCollector.instance.mostRecentStartActivity)?.run {
            try {
                // 只有ActivityFrom不为null时才有意义用startActivityForResult才有意义
                if (urlParams.requestCode > 0 && activityFrom != null) {
                    startActivityForResult(intent, urlParams.requestCode)
                } else {
                    startActivity(intent)
                }
                return
            } catch (e: Exception) {
                LogUtils.e(
                    IUrlInterceptor.TAG,
                    "Cannot open activity for $urlParams, fromActivity=${activityFrom}",
                    e
                )
            }
        }

        return
    }
}
