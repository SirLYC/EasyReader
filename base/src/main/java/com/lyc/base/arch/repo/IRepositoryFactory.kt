package com.lyc.base.arch.repo

import com.lyc.appinject.annotations.Extension

/**
 * Created by Liu Yuchuan on 2020/1/18.
 */
@Extension
interface IRepositoryFactory {
    fun <T : IRepository> getRepository(clazz: Class<T>, vararg params: Any): T?
}
