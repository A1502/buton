package third.part.entity;

import lombok.Getter;
import lombok.Setter;
import third.part.annotation.DemoFlag;
import third.part.annotation.OnClass;

import java.util.Date;

@OnClass(remark = "this is my teacher demo.")
public class TeacherEntity extends BaseEntity {

    /**
     * 仅仅有getter不会被复制
     * 同时由于是override，在递归父类成员时会有两条getBirthDay同名的信息，
     * 它们的owner不一样。这并不影响逻辑
     */
    @Override
    public Date getBaseField3() {
        return new Date(444);
    }

    /**
     * 仅仅有getter不会被复制
     */
    public void getTeacherField1() {
    }

    /**
     * 没有同时具备getter和setter的字段不会被复制
     */
    private String teacherField2;

    /**
     * 静态字段不会被复制
     */
    private static String teacherField3;

    public static String getTeacherField3() {
        return teacherField3;
    }

    public static void setTeacherField3(String value) {
        teacherField3 = value;
    }

    /**
     * 这个字段具备完整的getter和setter，会被复制（除非忽略了）
     */
    @Getter
    @Setter
    @DemoFlag(demoNumber = 1, demoType = Integer.class)
    private Integer teacherField4;

    /**
     * （虽然并没有String  mock这个字段）
     * 但getTeacherField5和setTeacherField5满足属性有getter和setter的要求，故会复制为Pojo的属性
     */
    public String getTeacherField5() {
        return null;
    }

    public void setTeacherField5(String value) {

    }

}
