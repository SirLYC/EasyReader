package com.lyc.easyreader.base.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.lyc.appinject.CreateMethod
import com.lyc.appinject.annotations.ExtensionImpl
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.base.utils.notch.NotchTools

/**
 * Created by Liu Yuchuan on 2020/1/16.
 */
@ExtensionImpl(
    extension = IApplicationOnCreateListener::class,
    createMethod = CreateMethod.GET_INSTANCE
)
class ActivityCollector private constructor() : IApplicationOnCreateListener,
    Application.ActivityLifecycleCallbacks {

    private val createdActivity = arrayListOf<BaseActivity>()
    var currentForegroundActivity: BaseActivity? = null
        private set
    var mostRecentStartActivity: BaseActivity? = null
        private set

    companion object {
        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            ActivityCollector()
        }

        private const val TAG = "ActivityCollector"
    }

    fun hasCreatedActivityExcept(activity: BaseActivity): Boolean {
        return createdActivity.count {
            it != activity
        } > 0
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
        if (activity == currentForegroundActivity) {
            currentForegroundActivity = null
        }
    }

    override fun onActivityStarted(activity: Activity) {
        doIfIsBaseActivity(activity, "onActivityStarted") {
            mostRecentStartActivity = it
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        doIfIsBaseActivity(activity, "onActivityDestroyed") {
            createdActivity.remove(it)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityStopped(activity: Activity) {
        if (mostRecentStartActivity == activity) {
            mostRecentStartActivity = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        doIfIsBaseActivity(activity, "onActivityCreated") {
            NotchCompat.instance.run {
                if (!notchInfoConvinced && !postSetNotchDevice) {
                    postSetNotchDevice = true
                    activity.window.decorView.post {
                        notchInfoConvinced = true
                        notchDevice =
                            NotchTools.getFullScreenTools().isNotchScreen(activity.window)
                        notchHeight =
                            NotchTools.getFullScreenTools().getNotchHeight(activity.window)
                        runPendingCommands()
                        LogUtils.i(
                            TAG,
                            "IsNotchDevice convinced! IsNotchDevice=${notchDevice}, notchHeight=${notchHeight}"
                        )
                    }
                }
            }

            createdActivity.add(it)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        doIfIsBaseActivity(activity, "onActivityResumed") {
            currentForegroundActivity = it
        }
    }
}
