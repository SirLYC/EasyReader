package com.lyc.appinject;

import com.lyc.appinject.annotations.ExtensionImp;
import com.lyc.appinject.annotations.ServiceImp;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
                    Class<?> clazz = ModuleApiHolders.getInstance().getClassForService(serviceClass);
                    if (clazz != null) {
                        CreateMethod method = CreateMethod.NEW;
                        for (Annotation annotation : clazz.getAnnotations()) {
                            if (annotation instanceof ServiceImp) {
                                method = ((ServiceImp) annotation).createMethod();
                            }
                        }
                        serviceImp = createNewInstance(clazz, method);
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
                    List<Class<?>> classes = ModuleApiHolders.getInstance().getClassesForExtension(extensionClass);
                    if (classes != null) {
                        for (Class<?> clazz : classes) {
                            CreateMethod method = CreateMethod.NEW;
                            for (Annotation annotation : clazz.getAnnotations()) {
                                if (annotation instanceof ExtensionImp) {
                                    method = ((ExtensionImp) annotation).createMethod();
                                    break;
                                }
                            }
                            Object instance = createNewInstance(clazz, method);
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

    private <T> T createNewInstance(Class<T> clazz, CreateMethod method) {
        switch (method) {
            case NEW:
                return newInstance(clazz);
            case GET_INSTANCE:
                return staticGetInstance(clazz);
        }

        return null;
    }

    private <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T staticGetInstance(Class<T> clazz) {
        try {
            Method method = clazz.getMethod("getInstance");
            return (T) method.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
