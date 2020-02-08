package com.lyc.easyreader.bookshelf.reader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.graphics.Color
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.app.NotchCompat
import com.lyc.easyreader.base.arch.provideViewModel
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.ui.getDrawableAttrRes
import com.lyc.easyreader.base.ui.theme.color_primary_text
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.utils.*
import com.lyc.easyreader.bookshelf.BuildConfig
import com.lyc.easyreader.bookshelf.R
import com.lyc.easyreader.bookshelf.reader.page.PageLoader
import com.lyc.easyreader.bookshelf.reader.page.PageView
import com.lyc.easyreader.bookshelf.reader.page.anim.PageAnimMode
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettings
import com.lyc.easyreader.bookshelf.reader.settings.ScreenOrientation
import kotlinx.android.synthetic.main.layout_reader_test_panel.*
import kotlinx.android.synthetic.main.layout_reader_test_panel.view.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
class ReaderActivity : BaseActivity(), PageView.TouchListener, View.OnClickListener {
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

        private val BRIGHTNESS_MODE_URI =
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE)
        private val BRIGHTNESS_URI = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS)
        private val BRIGHTNESS_ADJ_URI = Settings.System.getUriFor("screen_auto_brightness_adj")

        private const val SETTING_ANIM_TIME = 300L

        private val VIEW_ID_SETTING_BLANK = generateNewViewId()
        private val VIEW_ID_PRE_CHAP = generateNewViewId()
        private val VIEW_ID_NEXT_CHAP = generateNewViewId()
        private val VIEW_ID_BTN_CATEGORY = generateNewViewId()
        private val VIEW_ID_BTN_NIGHT_MODE = generateNewViewId()
        private val VIEW_ID_BTN_SETTINGS = generateNewViewId()
    }

    private lateinit var viewModel: ReaderViewModel
    private lateinit var contentView: FrameLayout
    private var pageLoader: PageLoader? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var contentObserver: ContentObserver? = null
    private var contentObserverRegistered = false
    // left -> top -> right -> bottom
    private val marginExtra = IntArray(4)
    private var isResume = false

    private val bottomInAnim by lazy {
        TranslateAnimation(
            Animation.ABSOLUTE,
            0f,
            Animation.ABSOLUTE,
            0f,
            Animation.RELATIVE_TO_SELF,
            1f,
            Animation.RELATIVE_TO_SELF,
            0f
        ).apply {
            duration = SETTING_ANIM_TIME
            interpolator = AccelerateInterpolator()
        }
    }

    private val bottomOutAnim by lazy {
        TranslateAnimation(
            Animation.ABSOLUTE,
            0f,
            Animation.ABSOLUTE,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            1f
        ).apply {
            duration = SETTING_ANIM_TIME
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    private val topInAnim by lazy {
        TranslateAnimation(
            Animation.ABSOLUTE,
            0f,
            Animation.ABSOLUTE,
            0f,
            Animation.RELATIVE_TO_SELF,
            -1f,
            Animation.RELATIVE_TO_SELF,
            0f
        ).apply {
            duration = SETTING_ANIM_TIME
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    private val topOutAnim by lazy {
        TranslateAnimation(
            Animation.ABSOLUTE,
            0f,
            Animation.ABSOLUTE,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            -1f
        ).apply {
            duration = SETTING_ANIM_TIME
            interpolator = AccelerateInterpolator()
        }
    }

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

        contentView = FrameLayout(this)
        rootView.addView(contentView)

        val page = PageView(this).apply {
            setTouchListener(this@ReaderActivity)
            contentView.addView(this)
        }
        val loader = page.getPageLoader(viewModel.bookFile)
        pageLoader = loader
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

        contentObserver = BrightnessObserver()

        registerSettings()
        initSettingView()

        if (BuildConfig.READER_TEST_MODE) {
            testControlPanel(rootView)
        }
    }


    override fun onStart() {
        super.onStart()
        if (!contentObserverRegistered) {
            val cr = contentResolver
            contentObserver?.let {
                cr.registerContentObserver(BRIGHTNESS_MODE_URI, false, it)
                cr.registerContentObserver(BRIGHTNESS_URI, false, it)
                cr.registerContentObserver(BRIGHTNESS_ADJ_URI, false, it)
            }
            contentObserverRegistered = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (contentObserverRegistered) {
            contentObserver?.let {
                contentResolver.unregisterContentObserver(it)
            }
            contentObserverRegistered = false
        }
    }

    private fun initSettingView() {
        val settingContentView = LinearLayout(this)
        settingContentView.orientation = LinearLayout.VERTICAL
        contentView.addView(settingContentView)
        val settingBlankView = View(this)
        settingBlankView.setBackgroundColor(0x00000000)
        settingContentView.addView(settingBlankView, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
        settingBlankView.id = VIEW_ID_SETTING_BLANK
        settingBlankView.setOnClickListener(this)

        val bottomButtons = arrayOf(
            Triple(VIEW_ID_BTN_CATEGORY, R.drawable.ic_format_list_bulleted_24dp, "目录"),
            Triple(VIEW_ID_BTN_CATEGORY, R.drawable.ic_half_moon_24dp, "夜间"),
            Triple(VIEW_ID_BTN_CATEGORY, R.drawable.ic_settings_24dp, "设置")
        )

        val bottomMenu = FrameLayout(this)
        bottomMenu.isClickable = true
        bottomMenu.isFocusable = true
        bottomMenu.setBackgroundColor(Color.WHITE)
        bottomMenu.setPadding(dp2px(16))
        settingContentView.addView(
            bottomMenu,
            LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM
            })

        val bottomMenuThemeColor = color_primary_text
        val dp56 = dp2px(56)
        val bottomButtonBar = LinearLayout(this)
        bottomButtonBar.orientation = LinearLayout.HORIZONTAL
        bottomMenu.addView(bottomButtonBar, FrameLayout.LayoutParams(MATCH_PARENT, dp56).apply {
            gravity = Gravity.BOTTOM
        })
        bottomButtons.forEach { buttonInfo ->
            val frameLayout = FrameLayout(this)
            frameLayout.id = buttonInfo.first
            bottomButtonBar.addView(frameLayout, LinearLayout.LayoutParams(0, MATCH_PARENT, 1f))
            val tv = TextView(this)
            tv.setTextColor(bottomMenuThemeColor)
            tv.text = buttonInfo.third
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(12f))
            tv.maxLines = 1
            tv.setSingleLine()
            tv.gravity = Gravity.CENTER or Gravity.BOTTOM
            val textHeight = tv.paint.let {
                val fontMetrics = it.fontMetrics
                return@let fontMetrics.descent - fontMetrics.ascent
            }.toInt() + dp2px(4)
            frameLayout.addView(
                tv,
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, textHeight).apply {
                    gravity = Gravity.BOTTOM
                })
            val iconIv = ImageView(this)
            iconIv.scaleType = ImageView.ScaleType.CENTER_INSIDE
            iconIv.setImageDrawable(getDrawable(buttonInfo.second)?.apply {
                changeToColor(
                    bottomMenuThemeColor
                )
            })
            frameLayout.addView(
                iconIv,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    bottomMargin = textHeight
                })
        }

        val chapterControlBar = LinearLayout(this)
        chapterControlBar.orientation = LinearLayout.HORIZONTAL
        chapterControlBar.gravity = Gravity.CENTER

        bottomMenu.addView(
            chapterControlBar,
            FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                bottomMargin = dp56 + dp2px(16)
            })
        bottomMenu.elevation = dp2pxf(4f)

        val preChapterTv = TextView(this)
        preChapterTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(14f))
        preChapterTv.paint.isFakeBoldText = true
        preChapterTv.setTextColor(bottomMenuThemeColor)
        preChapterTv.background = getDrawableAttrRes(android.R.attr.selectableItemBackground)
        preChapterTv.text = "上一章"
        preChapterTv.id = VIEW_ID_PRE_CHAP
        preChapterTv.setOnClickListener(this)
        preChapterTv.setPadding(dp2px(4))
        chapterControlBar.addView(
            preChapterTv,
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        )

        val seekBar = SeekBar(this)
        seekBar.isClickable = true
        seekBar.isFocusable = true
        chapterControlBar.addView(
            seekBar,
            LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply {
                leftMargin = dp2px(8)
                rightMargin = dp2px(8)
            }
        )


        val nextChapterTv = TextView(this)
        nextChapterTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(14f))
        nextChapterTv.paint.isFakeBoldText = true
        nextChapterTv.setTextColor(bottomMenuThemeColor)
        nextChapterTv.background = getDrawableAttrRes(android.R.attr.selectableItemBackground)
        nextChapterTv.text = "下一章"
        nextChapterTv.id = VIEW_ID_NEXT_CHAP
        nextChapterTv.setOnClickListener(this)
        nextChapterTv.setPadding(dp2px(4))
        chapterControlBar.addView(
            nextChapterTv,
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        )

        val topBar = BaseToolBar(this)
        topBar.setBarClickListener(this)
        topBar.setTitle("")
        settingContentView.addView(
            topBar,
            0,
            LinearLayout.LayoutParams(MATCH_PARENT, topBar.getViewHeight())
        )

        viewModel.showMenu.observeEvent(this, Observer {
            topBar.paddingStatusBar = marginExtra[1] > 0
            if (it) {
                topBar.startAnimation(topInAnim)
                bottomMenu.startAnimation(bottomInAnim)
            } else {
                topBar.startAnimation(topOutAnim)
                bottomMenu.startAnimation(bottomOutAnim)
            }
        })

        viewModel.showMenu.observeState(this, Observer {
            applyStatusBarColorChange()
            topBar.isVisible = it
            topBar.paddingStatusBar = marginExtra[1] > 0
            bottomMenu.isVisible = it
            settingBlankView.isVisible = it
        })
    }

    private fun initChapterView() {
        val chapterView = FrameLayout(this)
        chapterView.isVisible = false
        contentView.addView(chapterView)
    }

    private fun registerSettings() {
        val settings = ReaderSettings.instance
        settings.screenOrientation.observe(this, Observer {
            if (it.orientationValue != requestedOrientation) {
                requestedOrientation = it.orientationValue
            }
        })

        settings.fullscreen.observe(this, Observer { fullscreen ->
            applyFullscreen(fullscreen)
        })

        settings.screenOrientation.observe(this, Observer {
            autoFitPageViewMargin(settings.fullscreen.value)
        })

        settings.fontSizeInDp.observe(this, Observer { sizeInDp ->
            pageLoader?.setContentTextSize(dp2px(sizeInDp))
        })

        settings.pageAnimMode.observe(this, Observer { animMode ->
            pageLoader?.setPageAnimMode(animMode)
        })

        settings.indentCount.observe(this, Observer { count ->
            pageLoader?.setIndent(count, settings.indentFull.value)
        })

        settings.indentFull.observe(this, Observer { full ->
            pageLoader?.setIndent(settings.indentCount.value, full)
        })

        settings.brightnessFollowSystem.observe(this, Observer { followSystem ->
            if (followSystem) {
                setDefaultBrightness(this)
            } else {
                setBrightness(this, settings.userBrightness.value)
            }
        })

        settings.userBrightness.observe(this, Observer {
            if (!settings.brightnessFollowSystem.value) {
                setBrightness(this, it)
            }
        })

        settings.keepScreenOn.observe(this, Observer {
            applyKeepScreenOnChange()
        })

        settings.lineSpaceFactor.observe(this, Observer {
            pageLoader?.lineSpaceFactor = it
        })

        settings.paraSpaceFactor.observe(this, Observer {
            pageLoader?.paraSpaceFactor = it
        })
    }

    override fun onResume() {
        isResume = true
        applyKeepScreenOnChange()
        super.onResume()
        applyFullscreen(ReaderSettings.instance.fullscreen.value)
        applyStatusBarColorChange()
    }

    override fun onPause() {
        isResume = false
        applyKeepScreenOnChange()
        super.onPause()
    }

    private fun applyKeepScreenOnChange() {
        if (isResume && ReaderSettings.instance.keepScreenOn.value) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun applyStatusBarColorChange() {
        val settings = ReaderSettings.instance
        if (settings.fullscreen.value) {
            return
        }

        if (viewModel.showMenu.state) {
            window.statusBarBlackText(true)
            return
        }

        window.statusBarBlackText(false)
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

                else -> {
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
        pageLoader?.destroy()
        broadcastReceiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }

    override fun prePage() {

    }

    override fun onTouch(): Boolean {
        return true
    }

    override fun center() {
        viewModel.showMenu.state = true
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

    private inner class BrightnessObserver : ContentObserver(null) {

        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange)
            if (selfChange || !ReaderSettings.instance.brightnessFollowSystem.value) return

            // 如果系统亮度改变，则修改当前 Activity 亮度
            if (BRIGHTNESS_MODE_URI == uri) {
                LogUtils.d(TAG, "亮度模式改变")
            } else if (BRIGHTNESS_URI == uri && isAutoBrightness()) {
                LogUtils.d(TAG, "亮度模式为手动模式 值改变")
                setBrightness(this@ReaderActivity, getScreenBrightness())
            } else if (BRIGHTNESS_ADJ_URI == uri && isAutoBrightness()) {
                Log.d(TAG, "亮度模式为自动模式 值改变")
                setDefaultBrightness(this@ReaderActivity)
            } else {
                LogUtils.d(TAG, "亮度调整 其他")
            }
        }
    }

    private fun testControlPanel(rootView: FrameLayout) {
        LayoutInflater.from(this).inflate(R.layout.layout_reader_test_panel, rootView, true)

        val settings = ReaderSettings.instance

        rootView.tv_title.setOnClickListener {
            layout_panel.isVisible = false
        }

        rootView.bt_default.setOnClickListener {
            settings.applyDefaultSettings()
        }

        // 第一行
        rootView.bt_screen_orientation.setOnClickListener {
            val screenOrientation = settings.screenOrientation.value
            settings.screenOrientation.value =
                ScreenOrientation.values()[(screenOrientation.ordinal + 1) % ScreenOrientation.values().size]
        }
        settings.screenOrientation.observe(this@ReaderActivity, Observer {
            rootView.bt_screen_orientation.text = settings.screenOrientation.value.displayName
        })

        rootView.bt_fullscreen.setOnClickListener {
            settings.fullscreen.value = !settings.fullscreen.value
        }
        settings.fullscreen.observe(this@ReaderActivity, Observer {
            rootView.bt_fullscreen.text = if (it) "全屏" else "非全屏"
        })

        rootView.bt_anim_mode.setOnClickListener {
            val pageAnimMode = settings.pageAnimMode.value
            settings.pageAnimMode.value =
                PageAnimMode.values()[(pageAnimMode.ordinal + 1) % PageAnimMode.values().size]
        }
        settings.pageAnimMode.observe(this@ReaderActivity, Observer {
            rootView.bt_anim_mode.text = it.displayName
        })

        rootView.bt_keep_screen_on.setOnClickListener {
            settings.keepScreenOn.value = !settings.keepScreenOn.value
        }

        settings.keepScreenOn.observe(this, Observer {
            rootView.bt_keep_screen_on.text = if (it) "屏幕常亮：开" else "屏幕常亮：关"
        })

        // 第二行
        rootView.bt_decrease_font.setOnClickListener {
            settings.fontSizeInDp.value -= 1
        }
        settings.fontSizeInDp.observe(this@ReaderActivity, Observer {
            rootView.tv_font_size.text = ("$it")
        })
        rootView.bt_increase_font.setOnClickListener {
            settings.fontSizeInDp.value += 1
        }

        // 第三行
        rootView.bt_indent_count_increase.setOnClickListener {
            settings.indentCount.value = min(settings.indentCount.value + 1, 8)
        }

        rootView.bt_indent_count_decrease.setOnClickListener {
            settings.indentCount.value = max(settings.indentCount.value - 1, 0)
        }

        rootView.bt_indent_char.setOnClickListener {
            settings.indentFull.value = !settings.indentFull.value
        }

        settings.indentCount.observe(
            this,
            Observer { rootView.tv_indent_count.text = it.toString() })
        settings.indentFull.observe(this, Observer {
            rootView.bt_indent_char.text = if (it) "全角" else "半角"
        })

        var isTracking = false
        var inListener = false
        // 第四行
        rootView.sb_brightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                inListener = true
                settings.userBrightness.value = progress
                inListener = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTracking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTracking = false
            }
        })
        settings.userBrightness.observe(this, Observer {
            if (inListener || isTracking) {
                return@Observer
            }
            rootView.sb_brightness.progress = it
        })

        rootView.bt_brightness_mode.setOnClickListener {
            settings.brightnessFollowSystem.value = !settings.brightnessFollowSystem.value
        }

        settings.brightnessFollowSystem.observe(this, Observer {
            rootView.bt_brightness_mode.text = if (it) "跟随系统" else "自定义"
        })


        // 第五行
        var isLineSpaceTracking = false
        var inLineSpaceListener = false
        rootView.sb_line_space.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                inLineSpaceListener = true
                settings.lineSpaceFactor.value = progress * 3.0f * 0.01f
                inLineSpaceListener = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isLineSpaceTracking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isLineSpaceTracking = false
            }
        })
        settings.lineSpaceFactor.observe(this, Observer {
            if (inLineSpaceListener || isLineSpaceTracking) {
                return@Observer
            }
            rootView.sb_line_space.progress = (it * 100 / 3.0f).roundToInt()
        })

        var isParaSpaceTracking = false
        var inParaSpaceListener = false
        rootView.sb_para_space.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                inParaSpaceListener = true
                settings.paraSpaceFactor.value = progress * 3.0f * 0.01f
                inParaSpaceListener = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isParaSpaceTracking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isParaSpaceTracking = false
            }
        })
        settings.paraSpaceFactor.observe(this, Observer {
            if (inParaSpaceListener || isParaSpaceTracking) {
                return@Observer
            }
            rootView.sb_para_space.progress = (it * 100 / 3.0f).roundToInt()
        })

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            VIEW_ID_SETTING_BLANK -> {
                viewModel.showMenu.state = false
            }

            VIEW_ID_NEXT_CHAP -> {
                pageLoader?.skipNextChapter()
            }

            VIEW_ID_PRE_CHAP -> {
                pageLoader?.skipPreChapter()
            }

            BaseToolBar.VIEW_ID_LEFT_BUTTON -> {
                onBackPressed()
            }
        }
    }
}
