package com.lyc.easyreader.bookshelf.reader.settings

import com.lyc.easyreader.base.preference.IPreference
import com.lyc.easyreader.base.preference.value.PrefValue
import com.lyc.easyreader.bookshelf.reader.page.PageStyle

/**
 * Created by Liu Yuchuan on 2020/2/8.
 */
class PageStylePrefValue(
    key: String,
    preference: IPreference
) : PrefValue<PageStyle>(key, PageStyle.BG_1, preference, { k, defVal ->
    val string = preference.getString(k, "")
    if (string != null) {
        try {
            val split = string.split("&")
            if (split.size == 3) {
                val id = split[0].toInt()
                PageStyle.innerStyles[id] ?: PageStyle(split[1].toInt(), split[2].toInt())
            } else {
                defVal
            }
        } catch (e: Exception) {
            defVal
        }
    } else {
        defVal
    }
}, { k, v ->
    preference.putString(k, "${v.id}&${v.fontColor}&${v.bgColor}")
}, null, null)
