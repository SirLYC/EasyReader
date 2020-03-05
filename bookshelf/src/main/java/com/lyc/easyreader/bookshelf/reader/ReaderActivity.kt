package com.lyc.easyreader.bookshelf.reader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.*
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
import com.lyc.easyreader.api.book.BookChapter
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.api.book.BookMark
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.app.NotchCompat
import com.lyc.easyreader.base.arch.provideViewModel
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.ui.bottomsheet.LinearDialogBottomSheet
import com.lyc.easyreader.base.ui.getDrawableAttrRes
import com.lyc.easyreader.base.ui.theme.NightModeManager
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.ui.widget.SimpleToolbar
import com.lyc.easyreader.base.utils.*
import com.lyc.easyreader.bookshelf.BookManager
import com.lyc.easyreader.bookshelf.R
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper
import com.lyc.easyreader.bookshelf.reader.bookmark.BookMarkDialog
import com.lyc.easyreader.bookshelf.reader.page.PageLoader
import com.lyc.easyreader.bookshelf.reader.page.PageView
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettings
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettingsDialog
import com.lyc.easyreader.bookshelf.utils.millisToString

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
class ReaderActivity : BaseActivity(),
    PageView.PageViewGestureListener, OnClickListener,
    PageLoader.OnPageChangeListener {
    companion object {
        private const val KEY_BOOK_FILE = "KEY_BOOK_FILE"
        private const val KEY_SECRET_MODE = "KEY_SECRET_MODE"

        fun openBookFile(bookFile: BookFile, secretMode: Boolean = false) {
            if (bookFile.id == null) {
                ReaderToast.showToast("记录不存在")
                return
            }
            val context = ReaderApplication.appContext()
            val intent = Intent(context, ReaderActivity::class.java).apply {
                putExtra(KEY_BOOK_FILE, bookFile)
                putExtra(KEY_SECRET_MODE, secretMode)
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

        private val VIEW_ID_PRE_CHAP = generateNewViewId()
        private val VIEW_ID_NEXT_CHAP = generateNewViewId()
        private val VIEW_ID_BTN_CATEGORY = generateNewViewId()
        private val VIEW_ID_BTN_BOOK_MARK = generateNewViewId()
        private val VIEW_ID_BTN_NIGHT_MODE = generateNewViewId()
        private val VIEW_ID_BTN_SETTINGS = generateNewViewId()
        private val VIEW_ID_COPY_SELECT = generateNewViewId()
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
    private var copySelectView: View? = null
    private var selectViewWidth = 0
    private var selectViewHeight = 0

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
                viewModel.init(this, intent?.getBooleanExtra(KEY_SECRET_MODE, false) == true)
            }
        } else if (!isCreateFromConfigChange) {
            viewModel.restoreState(savedInstanceState)
        }
        if (viewModel.bookFileLiveData.value == null) {
            ReaderToast.showToast("打开文件失败，请重试")
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState(outState)
    }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        if (viewModel.bookFileLiveData.value == null) {
            return
        }

        contentView = FrameLayout(this)
        rootView.addView(contentView)

        copySelectView = TextView(this@ReaderActivity).apply {
            text = "复制"
            setPadding(dp2px(8), dp2px(4), dp2px(8), dp2px(4))
            textSizeInDp = 16f
            background = buildCommonButtonBg(Color.BLACK)
            setTextColor(
                if (NightModeManager.nightModeEnable) blendColor(
                    Color.WHITE,
                    NightModeManager.NIGHT_MODE_MASK_COLOR
                ) else Color.WHITE
            )
            id = VIEW_ID_COPY_SELECT
            setOnClickListener(this@ReaderActivity)
            contentView.addView(this, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
            isVisible = false
            measure(
                MeasureSpec.makeMeasureSpec((1 shl 30) - 1, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec((1 shl 30) - 1, MeasureSpec.AT_MOST)
            )
            selectViewWidth = measuredWidth
            selectViewHeight = measuredHeight
        }

        // 这里new一个也没关系，因为后续的变化都可以赋值进去的
        val page = PageView(this, viewModel.bookFileLiveData.value ?: BookFile()).apply {
            setTouchListener(this@ReaderActivity)
            contentView.addView(this)
        }
        val loader = page.pageLoader
        loader.setOnPageChangeListener(this)
        viewModel.bookFileLiveData.observe(this, Observer {
            loader.bookFile?.set(it)
        })
        pageLoader = loader
        viewModel.loadingChapterListLiveData.observe(this, Observer { loading ->
            if (!loading) {
                if (viewModel.charOffsets.all { it != -1 } && viewModel.charOffsets[1] >= viewModel.charOffsets[0]) {
                    loader.skipToChapter(
                        viewModel.currentChapter.value,
                        viewModel.charOffsets[0],
                        viewModel.charOffsets[1]
                    )
                } else {
                    loader.skipToChapter(viewModel.currentChapter.value)
                    loader.skipToPage(viewModel.currentPage.value)
                }
                loader.setChapterList(viewModel.bookChapterList)
                if (viewModel.parseResult != null && viewModel.parseResult!!.code != BookParser.CODE_SUCCESS) {
                    finish()
                }
            }
        })

        viewModel.changeChapterCall.observe(this, Observer {
            loader.skipToChapter(it)
        })
        viewModel.skipBookMarkCall.observe(this, Observer {
            loader.skipToChapter(it.first, it.second, it.third)
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
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 拦截音量键
        if (ReaderSettings.instance.volumeControlPage.value && keyCode in intArrayOf(
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_VOLUME_DOWN
            )
        ) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (viewModel.showMenu.changeState(true)) {
                return true
            }
        }

        val control = ReaderSettings.instance.volumeControlPage.value

        if (!control) {
            return super.onKeyUp(keyCode, event)
        }

        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (pageLoader?.skipToPrePage() != true) {
                    ReaderToast.showToast("已经是第一页了")
                }
                return true
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (pageLoader?.skipToNextPage() != true) {
                    ReaderToast.showToast("已经是最后一页了")
                }

                return true
            }
        }

        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            viewModel.showMenu.changeState(true)
            return true
        }
        return super.onKeyLongPress(keyCode, event)
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
        val settingContentView = FrameLayout(this)
        contentView.addView(settingContentView)
        val settingBlankView = FrameLayout(this)
        settingBlankView.setBackgroundColor(0x7f000000)
        settingContentView.addView(
            settingBlankView,
            FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        )
        settingBlankView.isFocusable = false
        settingBlankView.isClickable = false

        val bottomButtons = arrayOf(
            Triple(VIEW_ID_BTN_CATEGORY, R.drawable.ic_format_list_bulleted_24dp, "目录"),
            Triple(VIEW_ID_BTN_BOOK_MARK, R.drawable.ic_bookmark_border_24dp, "书签"),
            Triple(VIEW_ID_BTN_NIGHT_MODE, 0, "夜间"),
            Triple(VIEW_ID_BTN_SETTINGS, R.drawable.ic_settings_24dp, "设置")
        )

        val bottomMenu = FrameLayout(this)
        bottomMenu.isClickable = true
        bottomMenu.isFocusable = true
        bottomMenu.setPadding(dp2px(16))
        settingContentView.addView(
            bottomMenu,
            FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM
            })

        val dp56 = dp2px(56)
        val bottomButtonBar = LinearLayout(this)
        bottomButtonBar.orientation = LinearLayout.HORIZONTAL
        bottomMenu.addView(bottomButtonBar, FrameLayout.LayoutParams(MATCH_PARENT, dp56).apply {
            gravity = Gravity.BOTTOM
        })
        val bottomTvs = mutableListOf<TextView>()
        val bottomIcons = mutableListOf<Drawable>()
        bottomButtons.forEach { buttonInfo ->
            val frameLayout = FrameLayout(this)
            frameLayout.id = buttonInfo.first
            frameLayout.setOnClickListener(this)
            bottomButtonBar.addView(frameLayout, LinearLayout.LayoutParams(0, MATCH_PARENT, 1f))
            val tv = TextView(this)
            bottomTvs.add(tv)
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
            if (buttonInfo.second != 0) {
                iconIv.setImageDrawable(getDrawable(buttonInfo.second)?.apply {
                    bottomIcons.add(this)
                })
            }
            frameLayout.background = getDrawableAttrRes(android.R.attr.selectableItemBackground)
            frameLayout.addView(
                iconIv,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    bottomMargin = textHeight
                })

            if (buttonInfo.first == VIEW_ID_BTN_NIGHT_MODE) {
                val dayModeDrawable =
                    getDrawable(com.lyc.easyreader.api.R.drawable.ic_brightness_high_24dp)?.also {
                        bottomIcons.add(it)
                    }
                val nightModeDrawable =
                    getDrawable(com.lyc.easyreader.api.R.drawable.ic_half_moon_24dp)?.also {
                        bottomIcons.add(it)
                    }
                NightModeManager.nightMode.observe(this, Observer {
                    if (it) {
                        iconIv.setImageDrawable(dayModeDrawable)
                        tv.text = "日间"
                    } else {
                        iconIv.setImageDrawable(nightModeDrawable)
                        tv.text = "夜间"
                    }
                })
            }
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


        val pageCountView = TextView(this)
        pageCountView.setTextColor(Color.WHITE)
        pageCountView.paint.isFakeBoldText = true
        pageCountView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(14f))
        pageCountView.setPadding(dp2px(8), dp2px(4), dp2px(8), dp2px(4))
        pageCountView.isVisible = false
        pageCountView.background = PaintDrawable(Color.BLACK).apply {
            setCornerRadius(dp2pxf(4f))
        }

        settingBlankView.addView(
            pageCountView,
            FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER
                bottomMargin = dp2px(24)
            })

        viewModel.pageCount.observe(this, Observer {
            seekBar.max = it - 1
        })
        viewModel.currentPage.observe(this, Observer {
            seekBar.progress = it
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                pageCountView.text = ("${progress + 1}/${viewModel.pageCount.value}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                pageCountView.isVisible = true
                pageCountView.translationY = -bottomMenu.height.toFloat()
                pageCountView.text = ("${seekBar.progress + 1}/${viewModel.pageCount.value}")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                pageCountView.isVisible = false
                pageLoader?.skipToPage(seekBar.progress)
            }
        })
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
        nextChapterTv.background = getDrawableAttrRes(android.R.attr.selectableItemBackground)
        nextChapterTv.text = "下一章"
        nextChapterTv.id = VIEW_ID_NEXT_CHAP
        nextChapterTv.setOnClickListener(this)
        nextChapterTv.setPadding(dp2px(4))
        chapterControlBar.addView(
            nextChapterTv,
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        )

        val topBar = SimpleToolbar(this, com.lyc.easyreader.api.R.drawable.ic_more_horiz_24dp)
        topBar.setBarClickListener(this)
        settingContentView.addView(
            topBar,
            LinearLayout.LayoutParams(MATCH_PARENT, topBar.getViewHeight())
        )

        // Kotlin SAM-Lambda optimization
        try {
            ReaderTimeManager.readTimeThisSession.observe(this, Observer {
                LogUtils.d(TAG, "本次阅读时间更新：${it.millisToString()}")
            })
        } catch (e: Exception) {
            // ignore
        }

        ReaderTimeManager.readTimeTodayLiveData.observe(this, Observer {
            topBar.setTitle("今日已读：${it.millisToString()}")
        })


        val themeChangeCallback = {
            val currentPageStyle = ReaderSettings.currentPageStyle
            topBar.setBackgroundColor(currentPageStyle.bgColor)
            topBar.titleTv.setTextColor(currentPageStyle.fontColor)
            val fontColor = currentPageStyle.fontColor
            topBar.leftButton?.drawable?.changeToColor(fontColor)
            topBar.rightButton.drawable?.changeToColor(fontColor)
            preChapterTv.setTextColor(fontColor)
            nextChapterTv.setTextColor(fontColor)
            seekBar.progressDrawable?.changeToColor(fontColor)
            seekBar.thumb?.changeToColor(fontColor)
            bottomMenu.setBackgroundColor(currentPageStyle.bgColor)
            bottomIcons.forEach {
                it.changeToColor(fontColor)
            }
            bottomTvs.forEach {
                it.setTextColor(fontColor)
            }
        }

        ReaderSettings.instance.pageStyle.observe(this, Observer { themeChangeCallback() })
        NightModeManager.nightMode.observe(this, Observer { themeChangeCallback() })

        viewModel.showMenu.observeEvent(this, Observer {
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
            applyFullscreen()
            topBar.isVisible = it
            bottomMenu.isVisible = it
            settingBlankView.isVisible = it
        })
    }

    private fun registerSettings() {
        val settings = ReaderSettings.instance
        settings.screenOrientation.observe(this, Observer {
            if (it.orientationValue != requestedOrientation) {
                requestedOrientation = it.orientationValue
            }
        })

        settings.fullscreen.observe(this, Observer {
            applyFullscreen()
        })

        settings.screenOrientation.observe(this, Observer {
            applyFullscreen()
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
            pageLoader?.lineSpaceFactor = it.factor
        })

        settings.paraSpaceFactor.observe(this, Observer {
            pageLoader?.paraSpaceFactor = it.factor
        })

        settings.pageStyle.observe(this, Observer {
            pageLoader?.setPageStyle(it)
            applyStatusBarColorChange()
        })

        settings.fontBold.observe(this, Observer {
            pageLoader?.isFontBold = it
        })

        settings.readerMargin.observe(this, Observer {
            applyFullscreen()
        })

        NightModeManager.nightMode.observe(this, Observer {
            pageLoader?.setNightMode(it)
            applyStatusBarColorChange()
        })

        // ---------------------------- 实验设置 ---------------------------- //
        settings.drawTextBound.observe(this, Observer {
            pageLoader?.setDrawTextBound(it)
        })
    }

    override fun onResume() {
        isResume = true
        applyKeepScreenOnChange()
        super.onResume()
        applyFullscreen()
        ReaderTimeManager.enterRead()
    }

    override fun onPause() {
        ReaderTimeManager.exitRead()
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
        val pageStyle = ReaderSettings.currentPageStyle
        window.navigationBarColor = pageStyle.bgColor
        window.statusBarBlackText(pageStyle.statusBarBlack)
        window.navigationBarBlackText(pageStyle.statusBarBlack)
    }

    private fun applyFullscreen() {
        val fullscreen = ReaderSettings.instance.fullscreen.value
        if (fullscreen && !viewModel.showMenu.state) {
            enterFullscreen()
        } else {
            exitFullscreen()
        }
        window.decorView.clearSystemUiVisibility(
            SYSTEM_UI_FLAG_HIDE_NAVIGATION,
            SYSTEM_UI_FLAG_IMMERSIVE
        )
        autoFitPageViewMargin(fullscreen)
        applyStatusBarColorChange()
    }

    private fun autoFitPageViewMargin(fullscreen: Boolean) {
        val margin = dp2px(ReaderSettings.instance.readerMargin.value.margin)
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
            margin + marginExtra[0],
            margin + marginExtra[1],
            margin + marginExtra[2],
            margin + marginExtra[3]
        )
    }

    override fun onDestroy() {
        pageLoader?.destroy()
        broadcastReceiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }

    override fun prePage() {

    }

    override fun handlePageViewClick(): Boolean {
        return viewModel.showMenu.changeState(false)
    }

    override fun enableTouch(): Boolean {
        return true
    }

    override fun centerClick() {
        viewModel.showMenu.changeState(true)
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
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging =
                        status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                    LogUtils.d(
                        TAG,
                        "ACTION_BATTERY_CHANGED: level=$level, status=${status}, isCharging=${isCharging}"
                    )
                    pageLoader?.updateBattery(
                        level,
                        isCharging
                    )
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

    override fun onClick(v: View?) {
        when (v?.id) {
            VIEW_ID_NEXT_CHAP -> {
                pageLoader?.skipNextChapter()
            }

            VIEW_ID_PRE_CHAP -> {
                pageLoader?.skipPreChapter()
            }

            BaseToolBar.VIEW_ID_LEFT_BUTTON -> {
                finish()
            }

            VIEW_ID_BTN_NIGHT_MODE -> {
                NightModeManager.nightMode.flip()
            }

            VIEW_ID_BTN_SETTINGS -> {
                viewModel.showMenu.changeState(false)
                ReaderSettingsDialog().showOneTag(supportFragmentManager)
            }

            VIEW_ID_BTN_CATEGORY -> {
                viewModel.showMenu.changeState(false)
                ChapterDialog().showOneTag(supportFragmentManager)
            }

            VIEW_ID_BTN_BOOK_MARK -> {
                viewModel.showMenu.changeState(false)
                BookMarkDialog().showOneTag(supportFragmentManager)
            }

            VIEW_ID_COPY_SELECT -> {
                val string = pageLoader?.selectedTextAndExitSelectMode
                if (string == null) {
                    LogUtils.w(TAG, "Select nothing")
                } else {
                    copyPlainText(string) {
                        ReaderToast.showToast("已复制到剪贴板")
                        LogUtils.d(TAG, "Copy text=$string")
                    }
                }
            }

            SimpleToolbar.VIEW_ID_RIGHT_BUTTON -> {
                viewModel.showMenu.changeState(false)
                val pageStyle = ReaderSettings.currentPageStyle
                val dialog = LinearDialogBottomSheet(this)
                val collected: Boolean
                val collectId =
                    if ((viewModel.collected).also { collected = it }) {
                        dialog.addItem("取消收藏", R.drawable.ic_star_24dp, pageStyle.fontColor)
                    } else {
                        dialog.addItem("加入收藏", R.drawable.ic_star_border_24dp, pageStyle.fontColor)
                    }
                val bookMarkId =
                    dialog.addItem("添加书签", R.drawable.ic_bookmark_border_24dp, pageStyle.fontColor)
                val shareId = dialog.addItem("分享", R.drawable.ic_share_24dp, pageStyle.fontColor)
                val exportId =
                    dialog.addItem("其他应用打开", R.drawable.ic_launch_24dp, color = pageStyle.fontColor)
                dialog.bgColor = pageStyle.bgColor
                dialog.show()
                dialog.itemClickListener = { id, _ ->
                    when (id) {
                        collectId -> {
                            viewModel.updateCollectState(!collected)
                        }
                        bookMarkId -> {
                            addBookMark()
                        }
                        shareId -> shareBookFileByOtherApp()
                        exportId -> openBookFileByOtherApp()
                    }
                }

            }
        }
    }

    override fun onBackPressed() {
        if (!viewModel.showMenu.changeState(false)) {
            super.onBackPressed()
        }
    }

    private fun addBookMark() {
        viewModel.bookFileLiveData.value?.let { bookFile ->
            val desc = pageLoader?.descForCurrentPage
            if (desc == null) {
                ReaderToast.showToast("添加失败")
                return@let
            }

            val chapter = viewModel.currentChapter.value
            if (chapter < 0 || chapter >= viewModel.bookChapterList.size) {
                return@let
            }
            val bookMark = BookMark(
                null,
                bookFile.id,
                chapter,
                viewModel.charOffsets[0],
                viewModel.charOffsets[1],
                viewModel.currentPage.value,
                System.currentTimeMillis(),
                viewModel.bookChapterList[chapter].title,
                desc
            )
            BookShelfOpenHelper.instance.addBookMark(bookMark)
            ReaderToast.showToast("已添加书签")
        }
    }

    override fun onPageChange(pos: Int) {
        viewModel.currentPage.value = pos
        val chapterPos = pageLoader?.chapterPos ?: -1
        pageLoader?.getPageOffset(pos, viewModel.charOffsets)
        viewModel.updateBookReadRecord(chapterPos, pos)
    }

    override fun onCategoryFinish(chapters: MutableList<BookChapter>?) {
    }

    override fun onCanOperateSelectTextChange(canOperateSelectText: Boolean) {
        if (!canOperateSelectText) {
            copySelectView?.isVisible = false
        } else {
            val pos = floatArrayOf(-1f, -1f, -1f, -1f, -1f, -1f, -1f, -1f)
            pageLoader?.getSelectPositions(pos)
            if (pos.all { it > 0 }) {
                copySelectView?.isVisible = true
                copySelectView?.bringToFront()
                var xOffset = 0f
                var yOffset: Float
                var findPosition = false
                if ((pos[1] - selectViewHeight - dp2pxf(6f)).also { yOffset = it } > 0) {
                    // 可以显示在前面选择tab的上面
                    // 在上面找一个合适的位置
                    findPosition = true
                    xOffset = if (pos[0] + selectViewWidth < contentView.width) {
                        pos[0]
                    } else {
                        (contentView.width - selectViewWidth).toFloat()
                    }
                }

                if (!findPosition && (pos[7] + dp2pxf(24f) + selectViewHeight).also {
                        yOffset = it - selectViewHeight
                    } < contentView.height) {
                    // 可以显示在后面选择tab的下面
                    // 在上面找一个合适的位置
                    findPosition = true
                    xOffset = if (pos[4] + selectViewWidth < contentView.width) {
                        pos[4]
                    } else {
                        (contentView.width - selectViewWidth).toFloat()
                    }
                }

                if (!findPosition) {
                    // 还没找到位置就直接居中了...
                    yOffset = (pos[5] + pos[1]) * 0.5f
                    xOffset = (contentView.width - selectViewWidth) * 0.5f
                }
                copySelectView?.translationX = xOffset
                copySelectView?.translationY = yOffset
            }
        }
    }

    override fun onChapterChange(pos: Int) {
        viewModel.currentChapter.value = pos
    }

    override fun onChapterOpen(pos: Int) {
        onChapterChange(pos)
    }

    override fun onPageCountChange(count: Int) {
        viewModel.pageCount.value = count
    }

    private fun openBookFileByOtherApp() {
        viewModel.bookFileLiveData.value?.let { BookManager.instance.openBookFileByOther(it) }
    }

    private fun shareBookFileByOtherApp() {
        viewModel.bookFileLiveData.value?.let { BookManager.instance.shareBookFile(it) }
    }
}
