package com.lyc.easyreader.base.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import com.lyc.easyreader.base.R
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.theme.NightModeManager
import com.lyc.easyreader.base.utils.LogUtils

/**
 * Created by Liu Yuchuan on 2020/1/29.
 */
class ReaderPopupMenu : PopupMenu {

    companion object {
        private val DEFAULT_STYLE = Style.NORMAL
        private const val TAG = "ReaderPopupMenu"
    }

    enum class Style(
        @StyleRes
        val dayRes: Int,
        @StyleRes
        val nightModeRes: Int
    ) {
        NORMAL(R.style.PopupMenuDayNormal, R.style.PopupMenuNightNormal),
        ORANGE(R.style.PopupMenuDayOrange, R.style.PopupMenuNightOrange);

        @StyleRes
        fun getTheme(): Int {
            return if (NightModeManager.nightModeEnable) {
                nightModeRes
            } else {
                dayRes
            }
        }
    }

    private val anchorView: View

    constructor(context: Context, anchor: View, style: Style = DEFAULT_STYLE) : super(
        ContextThemeWrapper(
            context,
            style.getTheme()
        ),
        anchor
    ) {
        anchorView = anchor
    }

    constructor(context: Context, anchor: View, gravity: Int, style: Style = DEFAULT_STYLE) : super(
        ContextThemeWrapper(
            context,
            style.getTheme()
        ),
        anchor,
        gravity
    ) {
        anchorView = anchor
    }

    constructor(
        context: Context,
        anchor: View,
        gravity: Int,
        popupStyleAttr: Int,
        popupStyleRes: Int,
        style: Style = DEFAULT_STYLE
    ) : super(
        ContextThemeWrapper(
            context,
            style.getTheme()
        ), anchor, gravity, popupStyleAttr, popupStyleRes
    ) {
        anchorView = anchor
    }

    fun addItemRes(
        itemId: Int,
        title: String,
        iconRes: Int = 0,
        groupId: Int = 0,
        order: Int = itemId
    ): MenuItem {
        return addItem(itemId, title, getDrawableRes(iconRes), groupId, order)
    }

    fun addItem(
        itemId: Int,
        title: String,
        icon: Drawable? = null,
        groupId: Int = 0,
        order: Int = itemId
    ): MenuItem {
        val menuItem = menu.add(groupId, itemId, order, title)
        if (icon != null) {
            menuItem.icon = icon
        }
        return menuItem
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun setIconEnable(enable: Boolean) {
        try {
            val m = menu.javaClass.getDeclaredMethod(
                "setOptionalIconsVisible",
                Boolean::class.javaPrimitiveType
            )
            m.isAccessible = true
            //传入参数
            m.invoke(menu, enable)
        } catch (e: Exception) {
            LogUtils.e(TAG, ex = e)
        }
    }

    fun showAtLocation(x: Int, y: Int) {
        try {
            val field = javaClass.superclass!!.getDeclaredField("mPopup")
            field.isAccessible = true
            val helper = field.get(this) as MenuPopupHelper
            helper.show(x, y)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}
