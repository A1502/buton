package com.wuxian.buton.pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Pojo 属性的标记,在Pojo类中定义的字段添加此注解则会自动生成getter,setter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface PojoProperty {
}
