package com.lyc.easyreader.base.ui.widget

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.lyc.easyreader.base.R
import com.lyc.easyreader.base.ui.theme.NightModeManager

/**
 * Created by Liu Yuchuan on 2020/1/29.
 */
class ReaderPopupMenu : PopupMenu {
    companion object {
        private fun getCurrentTheme() =
            if (NightModeManager.nightModeEnable) R.style.PopupMenuNight else R.style.PopupMenuDay
    }

    constructor(context: Context, anchor: View) : super(
        ContextThemeWrapper(
            context,
            getCurrentTheme()
        ),
        anchor
    )

    constructor(context: Context, anchor: View, gravity: Int) : super(
        ContextThemeWrapper(
            context,
            getCurrentTheme()
        ),
        anchor,
        gravity
    )

    constructor(
        context: Context,
        anchor: View,
        gravity: Int,
        popupStyleAttr: Int,
        popupStyleRes: Int
    ) : super(
        ContextThemeWrapper(
            context,
            getCurrentTheme()
        ), anchor, gravity, popupStyleAttr, popupStyleRes
    )

}
