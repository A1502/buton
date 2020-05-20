package com.wuxian.buton.pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RetentionPolicy.设置为SOURCE，是因为含有protoTypeClass有类型强依赖，需要在生成的源码中消除
 * 关于生成的源码中@Pojo会被替换为@PojoRemark，可以用于回溯原型。
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface Pojo {

    /**
     * 原型类，可以看作OOP里的基类。Pojo仅仅会"继承"原型类（包括它的基类）中所有的属性，
     * 包括但不限于字段，方法在内的成员不会被"继承"
     */
    Class<?> protoTypeClass();

    /**
     * 无须复制的属性
     */
    String[] ignoreProperties() default {};
}
