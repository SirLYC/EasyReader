package com.lyc.easyreader.bookshelf.reader.settings

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.ui.BaseBottomSheet
import com.lyc.easyreader.base.ui.getDrawableAttrRes
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.theme.color_orange
import com.lyc.easyreader.base.ui.theme.color_yellow
import com.lyc.easyreader.base.utils.*
import com.lyc.easyreader.bookshelf.reader.page.PageStyle
import com.lyc.easyreader.bookshelf.reader.settings.colorpicker.PageStyleColorPickerDialog

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
class ReaderSettingsDialog : BaseBottomSheet(), View.OnClickListener {

    companion object {
        private val ROW_HEIGHT = dp2px(48)

        private val VIEW_ID_FOLLOW_SYSTEM = generateNewViewId()
        private val VIEW_ID_BOLD = generateNewViewId()
        private val VIEW_ID_FONT_DECREASE = generateNewViewId()
        private val VIEW_ID_FONT_INCREASE = generateNewViewId()
        private val VIEW_ID_FONT_DEFAULT = generateNewViewId()
        private val VIEW_ID_USER_PAGE_STYLE = generateNewViewId()
        private val VIEW_ID_MORE_SETTING = generateNewViewId()
        private val VIEW_ID_FULL_SCREEN = generateNewViewId()
        private val VIEW_ID_SCREEN_ORIENTATION = generateNewViewId()
        private val VIEW_ID_SCREEN_ON = generateNewViewId()

        val themeColor = color_orange
        const val contentColor = Color.WHITE
        val selectColor = color_yellow
    }

    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val ctx = ReaderApplication.appContext()
        var currentHeight = 0
        val rootView = FrameLayout(ctx).apply {
            background = PaintDrawable(themeColor).apply {
                val r = dp2pxf(16f)
                setCornerRadii(floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f))
            }
        }
        rootView.setPadding(dp2px(16))

        for (i in 1..5) {
            LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                rootView.addView(
                    this,
                    FrameLayout.LayoutParams(MATCH_PARENT, ROW_HEIGHT).apply {
                        topMargin = currentHeight
                    })
                currentHeight += ROW_HEIGHT
                when (i) {
                    1 -> initRow1View()
                    2 -> initRow2View()
                    3 -> initRow3View()
                    4 -> initRow4View()
                    5 -> initRow5View()
                }
            }
        }

        return rootView
    }

    // 第一行 亮度
    private fun LinearLayout.initRow1View() {
        val settings = ReaderSettings.instance
        val dp24 = dp2px(24)

        addView(
            buildTextButton(VIEW_ID_SCREEN_ON).apply {
                text = ("常亮")
                ReaderSettings.instance.keepScreenOn.observe(this@ReaderSettingsDialog, Observer {
                    selectState = it
                })
            },
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                rightMargin = dp2px(16)
            }
        )

        addView(ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageDrawable(getDrawableRes(com.lyc.easyreader.api.R.drawable.ic_brightness_low_24dp)?.apply {
                changeToColor(contentColor)
            })

        }, LinearLayout.LayoutParams(dp24, dp24))

        addView(SeekBar(context).apply {
            progressDrawable?.changeToColor(contentColor)
            thumb?.changeToColor(contentColor)
            max = 0xff
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    settings.userBrightness.value = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
            settings.userBrightness.observe(this@ReaderSettingsDialog, Observer {
                progress = it
            })
        }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))

        addView(ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageDrawable(getDrawableRes(com.lyc.easyreader.api.R.drawable.ic_brightness_high_24dp)?.apply {
                changeToColor(contentColor)
            })

        }, LinearLayout.LayoutParams(dp24, dp24))

        addView(
            buildTextButton(VIEW_ID_FOLLOW_SYSTEM).apply {
                text = "系统"
                settings.brightnessFollowSystem.observe(this@ReaderSettingsDialog, Observer {
                    selectState = it
                })
            },
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                leftMargin = dp2px(16)
            }
        )
    }

    // 第二行 字体
    private fun LinearLayout.initRow2View() {
        val settings = ReaderSettings.instance

        addView(
            buildTextButton(VIEW_ID_BOLD).apply {
                text = ("粗体")
                ReaderSettings.instance.fontBold.observe(this@ReaderSettingsDialog, Observer {
                    selectState = it
                })
            },
            LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        )

        addView(
            buildTextButton(VIEW_ID_FONT_DECREASE).apply { text = ("Aa-") },
            LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply {
                leftMargin = dp2px(16)
            }
        )
        addView(TextView(context).apply {
            setTextColor(contentColor)
            textSizeInPx = dp2pxf(16f)
            paint.isFakeBoldText = true
            gravity = Gravity.CENTER
            settings.fontSizeInDp.observe(this@ReaderSettingsDialog, Observer {
                text = "$it"
            })
        }, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            leftMargin = dp2px(16)
            rightMargin = dp2px(16)
        })

        addView(
            buildTextButton(VIEW_ID_FONT_INCREASE).apply { text = ("Aa+") },
            LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        )

        addView(
            buildTextButton(VIEW_ID_FONT_DEFAULT).apply {
                text = ("默认")
                settings.fontSizeInDp.observe(this@ReaderSettingsDialog, Observer {
                    selectState = it == settings.fontSizeInDp.defaultValue
                })
            },
            LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply {
                leftMargin = dp2px(16)
            }
        )
    }

    // 第三行 样式
    private fun LinearLayout.initRow3View() {
        val userStyleButton = buildTextButton(VIEW_ID_USER_PAGE_STYLE).apply { text = "自定义" }
        addView(
            userStyleButton,
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        )

        addView(RecyclerView(context).apply {
            setPadding(0, dp2px(4), 0, dp2px(4))
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = PageStyleAdapter().also { adapter ->
                ReaderSettings.instance.pageStyle.observe(this@ReaderSettingsDialog, Observer {
                    adapter.notifyDataSetChanged()
                    userStyleButton.selectState = PageStyle.innerStyles[it.id] == null
                })
            }
        }, 0, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply {
            rightMargin = dp2px(16)
        })
    }

    // 第四行 翻页效果
    private fun LinearLayout.initRow4View() {
        val settings = ReaderSettings.instance

//        addView(TextView(context).apply {
//            setTextColor(contentColor)
//            textSizeInPx = dp2pxf(14f)
//            paint.isFakeBoldText = true
//            gravity = Gravity.CENTER
//            text = "翻页效果"
//        }, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
//            rightMargin = dp2px(16)
//        })

        addView(RecyclerView(context).apply {
            setPadding(0, dp2px(4), 0, dp2px(4))
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = PageAnimModeAdapter()
                .also { adapter ->
                    settings.pageAnimMode.observe(this@ReaderSettingsDialog, Observer {
                        adapter.notifyDataSetChanged()
                    })
                }
        }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
    }

    // 第5行 全屏/方向和更多
    private fun LinearLayout.initRow5View() {
        background = getDrawableAttrRes(android.R.attr.selectableItemBackground)

        addView(
            buildTextButton(VIEW_ID_FULL_SCREEN).apply {
                text = ("全屏")
                ReaderSettings.instance.fullscreen.observe(this@ReaderSettingsDialog, Observer {
                    selectState = it
                })
            },
            LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        )

        addView(TextView(context).apply {
            id = VIEW_ID_MORE_SETTING
            setOnClickListener(this@ReaderSettingsDialog)
            setTextColor(contentColor)
            setPadding(dp2px(8))
            background = getDrawableAttrRes(android.R.attr.selectableItemBackground)
            textSizeInPx = dp2pxf(16f)
            paint.isFakeBoldText = true
            gravity = Gravity.CENTER
            text = "更多设置"
            setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                getDrawableRes(com.lyc.easyreader.api.R.drawable.ic_keyboard_arrow_right_24dp),
                null
            )
        }, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            leftMargin = dp2px(16)
            rightMargin = dp2px(16)
        })

        addView(
            buildTextButton(VIEW_ID_SCREEN_ORIENTATION).apply {
                ReaderSettings.instance.screenOrientation.observe(
                    this@ReaderSettingsDialog,
                    Observer {
                        text = it.displayName
                    })
            },
            LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        )
    }

    private fun buildTextButton(id: Int): SelectButton {
        val button = SelectButton(
            ReaderApplication.appContext(),
            buildCommonButtonBg(contentColor, true),
            buildCommonButtonBg(selectColor, true),
            buildCommonButtonTextColor(contentColor),
            buildCommonButtonTextColor(selectColor)
        )
        button.setTextColor(buildCommonButtonTextColor(color = contentColor))
        button.setOnClickListener(this)
        button.id = id
        return button
    }

    override fun onClick(v: View?) {
        val settings = ReaderSettings.instance
        when (v?.id) {
            VIEW_ID_FOLLOW_SYSTEM -> {
                settings.brightnessFollowSystem.flip()
            }
            VIEW_ID_BOLD -> {
                settings.fontBold.flip()
            }
            VIEW_ID_FONT_DECREASE -> {
                settings.fontSizeInDp.dec()
            }
            VIEW_ID_FONT_INCREASE -> {
                settings.fontSizeInDp.inc()
            }
            VIEW_ID_FONT_DEFAULT -> {
                settings.fontSizeInDp.applyDefaultValue()
            }
            VIEW_ID_USER_PAGE_STYLE -> {
                dismiss()
                activity?.supportFragmentManager?.let {
                    PageStyleColorPickerDialog().show(it)
                }
            }
            VIEW_ID_FULL_SCREEN -> {
                settings.fullscreen.flip()
            }
            VIEW_ID_MORE_SETTING -> {
                dismiss()
                ReaderApplication.appContext().apply {
                    startActivity(Intent(this, ReaderSettingsActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            }
            VIEW_ID_SCREEN_ON -> {
                settings.keepScreenOn.flip()
            }
            VIEW_ID_SCREEN_ORIENTATION -> {
                settings.screenOrientation.value = settings.screenOrientation.value.nextValue()
            }
        }
    }

    class SelectButton(
        context: Context?,
        private val commonDrawable: Drawable,
        private val selectDrawable: Drawable,
        private val commonTextColor: ColorStateList,
        private val selectTextColor: ColorStateList
    ) : TextView(context) {

        var selectState = false
            set(value) {
                if (value != field) {
                    background = if (value) {
                        setTextColor(selectTextColor)
                        selectDrawable
                    } else {
                        setTextColor(commonTextColor)
                        commonDrawable
                    }
                    field = value
                }
            }

        init {
            background = commonDrawable
            setTextColor(commonTextColor)
            textSizeInPx = dp2pxf(14f)
            setPadding(dp2px(8))
            paint.isFakeBoldText = true
            isClickable = true
            isFocusable = true
            paint.isFakeBoldText = true
            minWidth = dp2px(56)
            gravity = Gravity.CENTER
        }
    }
}
