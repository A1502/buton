package com.wuxian.buton.pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解@Pojo在生成后的源码中会被替换为这个注解，@PojoRemark采用了protoTypeClassName String类型而非和实际的prototype型形成依赖
 * RetentionPolicy.设置为RUNTIME,是为了生成的类可以用反射回溯获取prototype，为扩展逻辑提供机会
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PojoRemark {

    /**
     * 用字符串而不用Class<?>存储protoTypeClass，是为了生成类不对protoType产生依赖
     */
    String protoTypeClassName();

    /**
     * 无须复制的属性
     */
    String[] ignoreProperties() default {};
}
