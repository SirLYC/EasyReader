package com.lyc.easyreader.api.settings

import com.lyc.appinject.annotations.InjectApi


/**
 * Created by Liu Yuchuan on 2020/2/6.
 */
@InjectApi(oneToMany = true)
interface ISettings {
    fun applyDefaultSettings()
}
