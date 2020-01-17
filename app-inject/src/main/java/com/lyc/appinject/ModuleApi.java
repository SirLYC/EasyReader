package com.lyc.appinject;

import com.lyc.appinject.impl.Implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Liu Yuchuan on 2020/1/12.
 */
public class ModuleApi {
    private static final Lock INSTANCE_LOCK = new ReentrantLock();
    private volatile static ModuleApi instance;
    private ReadWriteLock serviceReadWriteLock = new ReentrantReadWriteLock();
    private Map<Class<?>, Object> serviceCache = new HashMap<>();

    private ReadWriteLock extensionReadWriteLock = new ReentrantReadWriteLock();
    private Map<Class<?>, List<?>> extensionCache = new HashMap<>();

    private ModuleApi() {

    }

    public static ModuleApi getInstance() {
        if (instance == null) {
            try {
                INSTANCE_LOCK.lock();
                if (instance == null) {
                    instance = new ModuleApi();
                }
            } finally {
                INSTANCE_LOCK.unlock();
            }
        }

        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        Object serviceImp;
        Lock readLock = serviceReadWriteLock.readLock();
        try {
            readLock.lock();
            serviceImp = serviceCache.get(serviceClass);
        } finally {
            readLock.unlock();
        }

        if (serviceImp == null) {
            Lock writeLock = serviceReadWriteLock.writeLock();
            try {
                writeLock.lock();
                serviceImp = serviceCache.get(serviceClass);
                if (serviceImp == null) {
                    Implementation impl = ModuleApiHolders.getInstance().getImplForService(serviceClass);
                    if (impl != null) {
                        serviceImp = impl.createInstance();
                    }
                }
                if (serviceImp != null) {
                    serviceCache.put(serviceClass, serviceImp);
                }
            } finally {
                writeLock.unlock();
            }
        }

        return (T) serviceImp;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getExtensions(Class<T> extensionClass) {
        List<T> result = new ArrayList<>();
        boolean hasCache;
        Lock readLock = extensionReadWriteLock.readLock();
        try {
            readLock.lock();
            List<?> extensionList = extensionCache.get(extensionClass);
            hasCache = extensionList != null;
            if (hasCache) {
                for (Object o : extensionList) {
                    result.add((T) o);
                }
            }
        } finally {
            readLock.unlock();
        }

        if (!hasCache) {
            Lock writeLock = extensionReadWriteLock.writeLock();
            try {
                writeLock.lock();
                List<?> extensionList = extensionCache.get(extensionClass);
                hasCache = extensionList != null;
                if (hasCache) {
                    for (Object o : extensionList) {
                        result.add((T) o);
                    }
                } else {
                    List<Implementation> impls = ModuleApiHolders.getInstance().getImplsForExtension(extensionClass);
                    if (impls != null) {
                        for (Implementation impl : impls) {
                            Object instance = impl.createInstance();
                            if (instance != null) {
                                result.add((T) instance);
                            }
                        }
                        extensionCache.put(extensionClass, Collections.unmodifiableList(result));
                    }
                }
            } finally {
                writeLock.unlock();
            }
        }

        return result;
    }

}
