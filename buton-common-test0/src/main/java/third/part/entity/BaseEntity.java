package third.part.entity;

import lombok.Getter;
import lombok.Setter;
import third.part.annotation.DemoFlag;
import third.part.annotation.RetentionClass;
import third.part.annotation.RetentionRunTime;
import third.part.annotation.RetentionSource;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
/**
 * 因为全局getter和setter的设置，这个类全部的字段都作为属性被复制到Pojo目标中
 */
public abstract class BaseEntity {

    private static final long serialVersionUID = -7226529037998960372L;

    @DemoFlag(demoNumber = 1, demoType = Integer.class)
    private String baseField1;

    /**
     * RetentionPolicy.SOURCE级别无条件的永远不会被复制
     */
    @RetentionSource
    @RetentionClass
    @RetentionRunTime
    private Date baseField2;

    public Date getBaseField3() {
        return new Date();
    }

    @DemoFlag(demoNumber = 1, demoType = Integer.class)
    private int baseField4;

    /**
     * 把这个字段按ignore properties做测试，故不会被复制
     */
    @DemoFlag(demoNumber = 1, demoType = Integer.class)
    private long baseField5;

    @DemoFlag(demoNumber = 1, demoType = Integer.class)
    private double baseField6;

    /**
     * 把这个字段按ignore properties做测试，故不会被复制
     */
    @DemoFlag(demoNumber = 1, demoType = Integer.class)
    private Integer baseField7;

    /**
     * 把这个字段按PojoPropertyMapping映射后重新定义了，故不会被复制，只复制上面的注解
     */
    @DemoFlag(demoNumber = 1, demoType = Integer.class)
    @RetentionRunTime
    @RetentionClass
    private Long baseField8;

    //测试无DemoFlag
    private Double baseField9;

    //测试实体类
    private EmployeeEntity baseField10;

    //测试list
    private List<EmployeeEntity> baseField11;

    //测试map
    private Map<String, EmployeeEntity> baseField12;

    private Map<String, List<EmployeeEntity>> baseField13;
}
