package com.lyc.easyreader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import androidx.core.util.valueIterator
import com.lyc.easyreader.api.book.IBookManager
import com.lyc.easyreader.api.main.IMainActivityDelegate
import com.lyc.easyreader.api.main.IMainTabDelegate
import com.lyc.easyreader.api.main.ITabChangeListener
import com.lyc.easyreader.api.main.Schema
import com.lyc.easyreader.base.getAppExtensions
import com.lyc.easyreader.base.getAppService
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.BaseFragment
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.generateNewRequestCode
import com.lyc.easyreader.base.utils.generateNewViewId

class MainActivity : BaseActivity(), ITabChangeListener {

    companion object {
        const val TAG = "MainActivity"

        val FRAGMENT_CONTAINER_ID = generateNewViewId()

        const val KEY_CURRENT_TAB_ID = "KEY_CURRENT_TAB_ID"

        val REQUEST_CODE_IMPORT_FILE_SCHEMA_URI = generateNewRequestCode()
    }

    private lateinit var container: FrameLayout
    private lateinit var bottomBar: HomeBottomBar
    private var pendingImportUri: Uri? = null
    private val mainTabs = SparseArray<IMainTabDelegate>().apply {
        for (tabDelegate in getAppExtensions<IMainTabDelegate>()) {
            put(tabDelegate.getId(), tabDelegate)
        }
    }
    private val bookManager by lazy { getAppService<IBookManager>() }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        container = FrameLayout(this)
        container.id = FRAGMENT_CONTAINER_ID

        container = FrameLayout(this)
        container.id = FRAGMENT_CONTAINER_ID

        val dp56 = dp2px(48)
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

        if (intent?.action in setOf(
                Intent.ACTION_SEND,
                Intent.ACTION_VIEW
            ) && intent?.data?.let { handleImportUri(it) } == true
        ) {
            LogUtils.d(TAG, "onCreate handle book import.")
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
        if (intent?.action in setOf(
                Intent.ACTION_SEND,
                Intent.ACTION_VIEW
            ) && intent?.data?.let { handleImportUri(it) } == true
        ) {
            LogUtils.d(TAG, "New intent handled by import book.")
            return
        }
        val targetTabId = intent?.data?.let { getTargetTabIdFromIntent(it) }
        intent?.data = null
        if (targetTabId != null) {
            bottomBar.changeTab(targetTabId)
        }
    }

    private fun handleImportUri(uri: Uri): Boolean {
        val scheme = uri.scheme
        if (scheme !in setOf("file", "content")) {
            return false
        }

        if (scheme == "file") {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                pendingImportUri = uri
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_IMPORT_FILE_SCHEMA_URI
                )
                return true
            }
        }

        bookManager?.importBooks(listOf(uri))
        return true
    }

    override fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == REQUEST_CODE_IMPORT_FILE_SCHEMA_URI) {
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    LogUtils.d(TAG, "Permission denied. Cannot import uri=${pendingImportUri}")
                    ReaderToast.showToast("导入失败")
                    return true
                }
            }
            bookManager?.run {
                pendingImportUri?.let {
                    if (it.scheme == "file") {
                        bookManager?.importBooks(listOf(it))
                    }
                }
            }
            pendingImportUri = null
            return true
        }

        return false
    }

    private fun getTargetTabIdFromIntent(dataFromIntent: Uri): Int? {
        val url = Uri.decode(dataFromIntent.toString())
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

    override fun onBackPressed() {
        for (tabDelegate in mainTabs.valueIterator()) {
            if (tabDelegate.onBackPressed()) {
                return
            }
        }
        super.onBackPressed()
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
        mainTabs[tabId]?.onThisTabClick()
    }
}
