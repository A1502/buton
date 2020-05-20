package com.wuxian.buton.pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface SupportedAnnotationTypes {
    /**
     * 复制protoType中的注解，包括类和字段(必须是属性的字段）上的注解，
     * 要强引用方式来保证对注解的依赖是完整的，所以这里是Class<?>[]存储的
     */
    Class<?>[] value();
}
