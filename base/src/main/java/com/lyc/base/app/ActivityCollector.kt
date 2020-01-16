package com.lyc.base.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImp
import com.lyc.base.ui.BaseActivity
import com.lyc.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/1/16.
 */
@ExtensionImp(
    extension = IApplicationOnCreateListener::class,
    createMethod = CreateMethod.GET_INSTANCE
)
class ActivityCollector private constructor() : IApplicationOnCreateListener,
    Application.ActivityLifecycleCallbacks {

    private val createdActivity = arrayListOf<BaseActivity>()
    var currentForegroundActivity: BaseActivity? = null
        private set

    companion object {
        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            ActivityCollector()
        }

        const val TAG = "ActivityCollector"
    }

    override fun onAppCreate(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    private inline fun doIfIsBaseActivity(
        activity: Activity,
        method: String,
        func: (BaseActivity) -> Unit
    ) {
        val baseActivity = activity as? BaseActivity
        if (baseActivity == null) {
            LogUtils.w(TAG, "[$method] $activity is not BaseActivity")
        } else {
            func(baseActivity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        doIfIsBaseActivity(activity, "onActivityDestroyed") {
            createdActivity.remove(it)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityStopped(activity: Activity) {
        if (activity == currentForegroundActivity) {
            currentForegroundActivity = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        doIfIsBaseActivity(activity, "onActivityCreated") {
            createdActivity.add(it)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        doIfIsBaseActivity(activity, "onActivityResumed") {
            currentForegroundActivity = it
        }
    }
}
