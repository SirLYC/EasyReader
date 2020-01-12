package com.lyc.appinject;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Liu Yuchuan on 2020/1/12.
 */
class ModuleApiHolders {

    private static final Lock INSTANCE_LOCK = new ReentrantLock();
    private static volatile ModuleApiHolders instance;
    private Map<Class<?>, Class<?>> serviceClassMap = new HashMap<>();

    private ModuleApiHolders() {
        initFields();
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
    private void initFields() {

    }

    Class<?> getClassForService(Class<?> serviceClazz) {
        return serviceClassMap.get(serviceClazz);
    }
}
