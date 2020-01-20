package com.lyc.appinject;

import com.lyc.appinject.impl.Implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Liu Yuchuan on 2020/1/12.
 */
class ModuleApiHolders {

    private static final Lock INSTANCE_LOCK = new ReentrantLock();
    private static volatile ModuleApiHolders instance;
    private Map<Class<?>, Implementation> serviceClassMap = new HashMap<>();
    private Map<Class<?>, List<Implementation>> extensionMap = new HashMap<>();

    private ModuleApiHolders() {
        initServices();
        initExtensions();
    }

    static ModuleApiHolders getInstance() {
        if (instance == null) {
            try {
                INSTANCE_LOCK.lock();
                if (instance == null) {
                    instance = new ModuleApiHolders();
                }
            } finally {
                INSTANCE_LOCK.unlock();
            }
        }

        return instance;
    }

    // 插桩方法
    private void initServices() {
    }

    // 插桩方法
    private void initExtensions() {
        List<Implementation> list;
    }

    Implementation getImplForService(Class<?> serviceClazz) {
        return serviceClassMap.get(serviceClazz);
    }

    List<Implementation> getImplsForExtension(Class<?> extensionClazz) {
        return extensionMap.get(extensionClazz);
    }
}