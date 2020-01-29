package com.lyc.base.ui.widget

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.lyc.base.R
import com.lyc.base.ui.theme.NightModeManager

/**
 * Created by Liu Yuchuan on 2020/1/29.
 */
class ReaderPopupMenu : PopupMenu {
    constructor(context: Context, anchor: View) : super(
        ContextThemeWrapper(
            context,
            if (NightModeManager.nightModeEnable) R.style.PopupMenuNight else R.style.PopupMenuDay
        ),
        anchor
    )

    constructor(context: Context, anchor: View, gravity: Int) : super(
        ContextThemeWrapper(
            context,
            if (NightModeManager.nightModeEnable) R.style.PopupMenuNight else R.style.PopupMenuDay
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
            if (NightModeManager.nightModeEnable) R.style.PopupMenuNight else R.style.PopupMenuDay
        ), anchor, gravity, popupStyleAttr, popupStyleRes
    )

}
