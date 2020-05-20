package test1.case3;

import com.wuxian.buton.pojo.Prototype;
import third.part.entity.generic.BaseGeneric;

/**
 * case3目标是测试泛型为原型
 */

反注释下面的内容，开始case3的解决
//@Pojo(protoTypeClass = Student.Inner.class)
public class Student {

    //@Prototype作为内嵌类，编译代码后会被清理
    //会以它extends的为准来记录到@PojoRemark的protoTypeClassName
    @Prototype
    public class Inner extends BaseGeneric<String> {
    }
}
