package com.wuxian.buton.pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RetentionPolicy.设置为RUNTIME,是为了生成的类可以用反射回溯
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface PojoPropertyMapping {
    String mappingTo();

    /**
     * 这注解和SupportedAnnotationTypes注解配合，
     * 按就近原则来决定字段的注解复制规则，
     * 即类级SupportedAnnotationTypes被覆盖，
     * 以字段级SupportedAnnotationTypes为准
     */
}
