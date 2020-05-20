package test1.case5;

import com.wuxian.buton.pojo.Pojo;


/**
 * case5目标是测试证明当前的module，lombok的注解(包括@Getter @Setter)和buton是不能同时生效的
 */

@Pojo(protoTypeClass = BaseHome.class)
public class Home extends BaseHome {
}
