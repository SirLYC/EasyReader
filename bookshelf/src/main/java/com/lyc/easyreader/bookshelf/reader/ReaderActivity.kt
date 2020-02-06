package com.lyc.easyreader.bookshelf.reader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.app.NotchCompat
import com.lyc.easyreader.base.arch.provideViewModel
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.getScreenOrientation
import com.lyc.easyreader.base.utils.statusBarHeight
import com.lyc.easyreader.bookshelf.BuildConfig
import com.lyc.easyreader.bookshelf.reader.page.PageLoader
import com.lyc.easyreader.bookshelf.reader.page.PageView
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettings
import com.lyc.easyreader.bookshelf.reader.settings.ScreenOrientation

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
class ReaderActivity : BaseActivity(), PageView.TouchListener {
    companion object {
        private const val KEY_BOOK_FILE = "KEY_BOOK_FILE"

        fun openBookFile(bookFile: BookFile) {
            if (bookFile.id == null) {
                ReaderToast.showToast("记录不存在")
                return
            }
            val context = ReaderApplication.appContext()
            val intent = Intent(context, ReaderActivity::class.java).apply {
                putExtra(KEY_BOOK_FILE, bookFile)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        const val TAG = "ReaderActivity"
    }

    private lateinit var viewModel: ReaderViewModel
    private var pageLoader: PageLoader? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    // left -> top -> right -> bottom
    private val marginExtra = IntArray(4)

    override fun beforeBaseOnCreate(savedInstanceState: Bundle?) {
        viewModel = provideViewModel()
        if (savedInstanceState == null) {
            intent?.getParcelableExtra<BookFile>(KEY_BOOK_FILE)?.run {
                viewModel.init(this)
            }
        } else if (!isCreateFromConfigChange) {
            viewModel.restoreState(savedInstanceState)
        }
        if (viewModel.bookFile == null) {
            ReaderToast.showToast("打开文件失败，请重试")
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState(outState)
    }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        if (viewModel.bookFile == null) {
            return
        }


        val page = PageView(this).apply {
            setTouchListener(this@ReaderActivity)
            rootView.addView(this)
        }
        val loader = page.getPageLoader(viewModel.bookFile)
        pageLoader = loader
        autoFitPageViewMargin(ReaderSettings.fullscreen.value)
        viewModel.loadingChapterListLiveData.observe(this, Observer { loading ->
            if (!loading) {
                loader.setChapterListIfEmpty(viewModel.bookChapterList)
            }
        })


        BatteryAndTimeReceiver().let {
            broadcastReceiver = it
            registerReceiver(it, IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_TIME_TICK)
            })
        }

        registerSettings()

        if (BuildConfig.READER_TEST_MODE) {
            testControlPanel(rootView)
        }
    }

    private fun registerSettings() {
        ReaderSettings.screenOrientation.observe(this, Observer {
            if (it.orientationValue != requestedOrientation) {
                requestedOrientation = it.orientationValue
            }
        })

        ReaderSettings.fullscreen.observe(this, Observer { fullscreen ->
            applyFullscreen(fullscreen)
        })
    }

    override fun onResume() {
        super.onResume()
        applyFullscreen(ReaderSettings.fullscreen.value)
    }

    private fun applyFullscreen(fullscreen: Boolean) {
        if (fullscreen) {
            enterFullscreen()
        } else {
            exitFullscreen()
        }
        autoFitPageViewMargin(fullscreen)
    }

    private fun autoFitPageViewMargin(fullscreen: Boolean) {
        val dp16 = dp2px(16)
        for (i in marginExtra.indices) {
            marginExtra[i] = 0
        }

        if (fullscreen && NotchCompat.instance.notchDevice) {
            // 全屏时需要考虑刘海屏
            when (getScreenOrientation()) {
                90 -> {
                    // 刘海在左边
                    marginExtra[0] = NotchCompat.instance.notchHeight
                }

                180 -> {
                    // 刘海在下面
                    marginExtra[3] = NotchCompat.instance.notchHeight
                }

                270 -> {
                    // 刘海在右边
                    marginExtra[2] = NotchCompat.instance.notchHeight
                }

                360 -> {
                    // 刘海在上面
                    marginExtra[1] = NotchCompat.instance.notchHeight
                }
            }
        } else if (!fullscreen) {
            // 全屏时需要考虑刘海和状态栏
            when (getScreenOrientation()) {
                90 -> {
                    // 状态栏
                    marginExtra[1] = statusBarHeight()
                    // 刘海在左边
                    marginExtra[0] = NotchCompat.instance.notchHeight
                }

                180 -> {
                    // 状态栏
                    marginExtra[1] = statusBarHeight()
                    // 刘海在下面
                    marginExtra[3] = NotchCompat.instance.notchHeight
                }

                270 -> {
                    // 状态栏
                    marginExtra[1] = statusBarHeight()
                    // 刘海在右边
                    marginExtra[2] = NotchCompat.instance.notchHeight
                }

                else -> {
                    // 刘海在上面
                    marginExtra[1] = statusBarHeight()
                }
            }
        }

        pageLoader?.setMargins(
            dp16 + marginExtra[0],
            dp16 + marginExtra[1],
            dp16 + marginExtra[2],
            dp16 + marginExtra[3]
        )
    }

    override fun onDestroy() {
        broadcastReceiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }

    override fun prePage() {

    }

    override fun onTouch(): Boolean {
        return true
    }

    override fun center() {

    }

    override fun cancel() {
    }

    override fun nextPage() {

    }

    private inner class BatteryAndTimeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    pageLoader?.updateBattery(level)
                }

                Intent.ACTION_TIME_TICK -> {
                    pageLoader?.updateTime()
                }
            }
        }
    }

    private fun testControlPanel(rootView: FrameLayout) {
        val controlPanel = LinearLayout(this)
        controlPanel.orientation = LinearLayout.VERTICAL
        rootView.addView(controlPanel, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        })
        val screenButton = Button(this)
        screenButton.setOnClickListener {
            val screenOrientation = ReaderSettings.screenOrientation.value
            ReaderSettings.screenOrientation.value =
                ScreenOrientation.values()[(screenOrientation.ordinal + 1) % ScreenOrientation.values().size]
        }
        controlPanel.addView(screenButton)
        ReaderSettings.screenOrientation.observe(this, Observer {
            screenButton.text = ReaderSettings.screenOrientation.value.displayName
        })

        val fullScreenButton = Button(this)
        fullScreenButton.setOnClickListener {
            ReaderSettings.fullscreen.value = !ReaderSettings.fullscreen.value
        }
        ReaderSettings.fullscreen.observe(this, Observer {
            fullScreenButton.text = if (it) "全屏" else "非全屏"
        })
        controlPanel.addView(fullScreenButton)
    }
}
