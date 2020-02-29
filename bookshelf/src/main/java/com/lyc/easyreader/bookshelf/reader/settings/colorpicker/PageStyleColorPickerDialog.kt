package com.lyc.easyreader.bookshelf.reader.settings.colorpicker

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import com.lyc.colorpicker.HueBarView
import com.lyc.colorpicker.SVColorPickerView
import com.lyc.easyreader.base.ui.bottomsheet.BaseBottomSheet
import com.lyc.easyreader.base.ui.theme.NightModeManager
import com.lyc.easyreader.bookshelf.R
import com.lyc.easyreader.bookshelf.reader.page.PageStyle
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettings
import kotlinx.android.synthetic.main.layout_color_picker_dialog.view.*

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
class PageStyleColorPickerDialog : BaseBottomSheet() {

    private val colorBgHSV = FloatArray(3).apply {
        Color.colorToHSV(ReaderSettings.instance.pageStyle.value.bgColor, this)
    }
    private val colorFontHSV = FloatArray(3).apply {
        Color.colorToHSV(ReaderSettings.instance.pageStyle.value.fontColor, this)
    }
    private lateinit var colorPickerView: SVColorPickerView
    private lateinit var huePickerView: HueBarView

    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.layout_color_picker_dialog, container, false)
        colorPickerView = rootView.cpv
        huePickerView = rootView.hpv
        colorPickerView.setColorPickerViewListener(object :
            SVColorPickerView.ColorPickerViewListener {
            override fun onColorChange(newColor: Int, colorHSV: FloatArray) {
                val target = if (rootView.rg.checkedRadioButtonId == R.id.rbtn_bg) {
                    colorBgHSV
                } else {
                    colorFontHSV
                }
                colorHSV.copyInto(target)
                NightModeManager.nightMode.value = false
                ReaderSettings.instance.pageStyle.value =
                    PageStyle(Color.HSVToColor(colorFontHSV), Color.HSVToColor(colorBgHSV))
            }
        })
        huePickerView.setOnHueBarChangeListener(object : HueBarView.OnHueBarChangeListener {
            override fun onHueChange(newHue: Float) {
                rootView.cpv.hue = newHue
            }
        })
        val target = if (rootView.rg.checkedRadioButtonId == R.id.rbtn_bg) {
            colorBgHSV
        } else {
            colorFontHSV
        }
        colorPickerView.setCurrentColorHSV(target)
        huePickerView.currentHue = target[0]
        rootView.rg.setOnCheckedChangeListener { _, checkedId ->
            val targetColor = if (checkedId == R.id.rbtn_bg) {
                colorBgHSV
            } else {
                colorFontHSV
            }
            colorPickerView.setCurrentColorHSV(targetColor)
            huePickerView.currentHue = targetColor[0]
        }
        return rootView
    }

    override fun changeWindowAndDialogAfterSetContent(dialog: Dialog, window: Window) {
        super.changeWindowAndDialogAfterSetContent(dialog, window)
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

}
