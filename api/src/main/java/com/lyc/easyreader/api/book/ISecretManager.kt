package com.lyc.easyreader.api.book

import com.lyc.appinject.annotations.InjectApi

/**
 * Created by Liu Yuchuan on 2020/3/9.
 */
@InjectApi
interface ISecretManager {
    fun resetPassword()

    fun resetChangeCnt(): Int

    fun hasPassword(): Boolean
}
