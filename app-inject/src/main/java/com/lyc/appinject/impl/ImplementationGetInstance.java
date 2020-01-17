package com.lyc.appinject.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Liu Yuchuan on 2020/1/17.
 */
public class ImplementationGetInstance extends Implementation {

    public ImplementationGetInstance(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public Object createInstance() {
        try {
            Method method = clazz.getMethod("getInstance");
            return method.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
