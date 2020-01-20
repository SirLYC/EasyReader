package com.lyc.easyreader

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.util.forEach
import com.lyc.api.main.IMainActivityDelegate
import com.lyc.api.main.IMainTabDelegate
import com.lyc.api.main.ITabChangeListener
import com.lyc.api.main.Schema
import com.lyc.base.getAppExtensions
import com.lyc.base.ui.BaseActivity
import com.lyc.base.ui.BaseFragment
import com.lyc.base.utils.LogUtils
import com.lyc.base.utils.dp2px
import com.lyc.base.utils.generateNewViewId

class MainActivity : BaseActivity(), ITabChangeListener {

    companion object {
        const val TAG = "MainActivity"

        val FRAGMENT_CONTAINER_ID = generateNewViewId()

        const val KEY_CURRENT_TAB_ID = "KEY_CURRENT_TAB_ID"
    }

    private lateinit var container: FrameLayout
    private lateinit var bottomBar: HomeBottomBar
    private val mainTabs = SparseArray<IMainTabDelegate>().apply {
        for (tabDelegate in getAppExtensions<IMainTabDelegate>()) {
            put(tabDelegate.getId(), tabDelegate)
        }
    }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: ViewGroup) {
        container = FrameLayout(this)
        container.id = FRAGMENT_CONTAINER_ID
        setContentView(container)

        container = FrameLayout(this)
        container.id = FRAGMENT_CONTAINER_ID
        container.setBackgroundColor(Color.WHITE)

        val dp56 = dp2px(56)
        rootView.addView(
            container,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                bottomMargin = dp56
            })
        rootView.addView(
            HomeBottomBar(this).also {
                bottomBar = it
            },
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp56
            ).apply {
                gravity = Gravity.BOTTOM
            }
        )

        MainActivityDelegate.instance.addTabChangeListener(this)

        val fm = supportFragmentManager
        if (savedInstanceState != null) {
            mainTabs.forEach { id, tab ->
                tab.recoverFragment(fm.findFragmentByTag(id.toString()))
            }
        }

        val dataFromIntent = intent?.data
        LogUtils.d(
            TAG,
            "[MainActivity] OnCreate...bundle=${savedInstanceState}, intent.data=${dataFromIntent}"
        )
        var targetTabId: Int? = null
        if (dataFromIntent != null) {
            targetTabId = getTargetTabIdFromIntent(dataFromIntent)
            intent.data = null
        }

        if (savedInstanceState != null && targetTabId == null) {
            val tabId = savedInstanceState.getInt(KEY_CURRENT_TAB_ID, 0)
            if (MainActivityDelegate.instance.isMainTabId(tabId)) {
                targetTabId = tabId
            }
        }

        if (targetTabId == null) {
            targetTabId = IMainActivityDelegate.ID_BOOK_SHELF
        }

        bottomBar.changeTab(targetTabId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_TAB_ID, bottomBar.currentId)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        LogUtils.d(
            TAG,
            "[MainActivity] onNewIntent...new intent.data=${intent?.data}"
        )
        val targetTabId = intent?.data?.let { getTargetTabIdFromIntent(it) }
        intent?.data = null
        if (targetTabId != null) {
            bottomBar.changeTab(targetTabId)
        }
    }

    private fun getTargetTabIdFromIntent(dataFromIntent: Uri): Int? {
        val url = dataFromIntent.toString()
        if (url.startsWith(Schema.MAIN_PAGE + "/")) {
            val tabPath = url.substring(Schema.MAIN_PAGE.length + 1).substringBefore("/", "")
            if (tabPath.isNotEmpty()) {
                val tabId = tabPath.toIntOrNull()
                if (tabId?.let { MainActivityDelegate.instance.isMainTabId(it) } == true) {
                    return tabId
                }
            }
        }

        return null
    }

    override fun onDestroy() {
        MainActivityDelegate.instance.removeTabChangeListener(this)
        super.onDestroy()
    }

    override fun onChangeToNewTab(tabId: Int) {
        LogUtils.d(
            TAG,
            "Tab change, new tab id=$tabId, name=${MainActivityDelegate.instance.tabIdToString(tabId)}"
        )

        val targetTab = mainTabs[tabId]
            ?: throw IllegalArgumentException(
                "Cannot find main tab id for {id= ${tabId}, name=${MainActivityDelegate.instance.tabIdToString(
                    tabId
                )}}"
            )

        mainTabs.forEach { key, value ->
            if (key != tabId) {
                value.onInvisible()
            } else {
                value.onVisible()
            }
        }
        val fm = supportFragmentManager
        if (fm.findFragmentByTag(tabId.toString()) as? BaseFragment == null) {
            fm.beginTransaction()
                .add(FRAGMENT_CONTAINER_ID, targetTab.createFragment(), tabId.toString())
                .commitNow()
        }
    }

    override fun onCurrentTabClick(tabId: Int) {
        LogUtils.d(
            TAG,
            "Tab click, click tab id=$tabId, name=${MainActivityDelegate.instance.tabIdToString(
                tabId
            )}"
        )
    }
}
