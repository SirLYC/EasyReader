package com.lyc.appinject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
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
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Map<Class<?>, Object> serviceCache = new HashMap<>();

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

    public <T> T getService(Class<T> serviceClass) {
        Object serviceImp;
        Lock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            serviceImp = serviceCache.get(serviceClass);
        } finally {
            readLock.unlock();
        }

        if (serviceImp == null) {
            Lock writeLock = readWriteLock.writeLock();
            try {
                writeLock.lock();
                serviceImp = serviceCache.get(serviceClass);
                if (serviceImp == null) {
                    Class<?> clazz = ModuleApiHolders.getInstance().getClassForService(serviceClass);
                    if (clazz != null) {
                        try {
                            serviceImp = clazz.getConstructor().newInstance();
                        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
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
}
