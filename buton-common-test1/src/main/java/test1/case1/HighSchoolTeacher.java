package test1.case1;

import com.wuxian.buton.pojo.Pojo;
import com.wuxian.buton.pojo.PojoProperty;
import com.wuxian.buton.pojo.PojoPropertyMapping;
import com.wuxian.buton.pojo.SupportedAnnotationTypes;
import lombok.Getter;
import third.part.annotation.DemoFlag;
import third.part.annotation.OnClass;
import third.part.annotation.RetentionRunTime;

@Pojo(protoTypeClass = third.part.entity.TeacherEntity.class, ignoreProperties = {"baseField7", "baseField5"})
@SupportedAnnotationTypes({RetentionRunTime.class, OnClass.class})
public class HighSchoolTeacher {

    /**
     * PojoProperty注解使得所有的字段都会被封装getter和setter
     * 因此最终代码里会有getPojoField1,setPojoField1
     */
    @PojoProperty
    private String pojoField1;

    public String getPojoField1() {
        return pojoField1;
    }

    /**
     * 虽然有lombok的getter，Pojo注解使得所有的字段都会被封装getter和setter
     * 因此最终代码里会有getPojoField2,setPojoField2
     */
    @Getter
    @PojoProperty
    private String pojoField2;

    /**
     * 改掉原型中的baseField8的定义(当然名字也可以改)，类型改为Runnable
     * 包括注解提取的范围只有DemoFlag，不再复制RetentionRunTime
     * PojoPropertyMapping.mappingTo保留了它和出处的关联信息
     */
    @SupportedAnnotationTypes({DemoFlag.class})
    @PojoPropertyMapping(mappingTo = "baseField8")
    private Runnable baseField8;

   /* private Runnable baseField9;

    private Runnable baseField9;*/
    /**
     * protoTypeClass中已经有这个定义，用于测试重复定义是否会报错
     */
    //private Double baseField9;
}
