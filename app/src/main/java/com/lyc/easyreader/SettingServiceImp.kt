package com.lyc.easyreader

import com.lyc.appinject.annotations.InjectApiImpl
import com.lyc.easyreader.api.settings.ISettingService
import com.lyc.easyreader.base.ReaderApplication

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
@InjectApiImpl(api = ISettingService::class)
class SettingServiceImp : ISettingService {
    override fun openSettingActivity() {
        ReaderApplication.openActivity(SettingActivity::class)
    }
}
