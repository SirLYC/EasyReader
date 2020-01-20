package com.lyc.appinject.annotations;

import com.lyc.appinject.CreateMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ExtensionImpl {

    Class<?> extension();

    CreateMethod createMethod() default CreateMethod.NEW;
}