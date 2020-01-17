package com.lyc.appinject.impl;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by Liu Yuchuan on 2020/1/17.
 */
public class ImplementationNew extends Implementation {

    public ImplementationNew(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public Object createInstance() {
        if (clazz == null) {
            return null;
        }

        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
