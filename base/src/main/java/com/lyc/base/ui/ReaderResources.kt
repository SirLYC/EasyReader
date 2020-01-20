package com.lyc.base.ui

import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.lyc.base.ReaderApplication

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
fun getStringRes(@StringRes resId: Int) = ReaderApplication.appContext().resources.getString(resId)

fun getDrawableRes(@DrawableRes resId: Int) =
    ContextCompat.getDrawable(ReaderApplication.appContext(), resId)

fun getDrawableAttrRes(@AttrRes resId: Int): Drawable? {
    val context = ReaderApplication.appContext()
    val typedValue = TypedValue()
    context.theme.resolveAttribute(
        resId,
        typedValue,
        true
    )
    val attribute = intArrayOf(resId)
    val typedArray = context.theme.obtainStyledAttributes(typedValue.resourceId, attribute)
    val drawable = typedArray.getDrawable(0)
    typedArray.recycle()
    return drawable
}
