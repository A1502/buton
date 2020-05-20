package test1.case2;

import com.wuxian.buton.pojo.Pojo;
import com.sun.tools.javac.util.List;
import test1.case1.HighSchoolTeacher;

import java.util.Set;

/**
 * case2目标是测试接口为原型
 */
@Pojo(protoTypeClass = TestInter2.class)
public class Worker {

    private List<Set<Long>> testGeneric;

    private int workerAge;

    private HighSchoolTeacher teacher = new HighSchoolTeacher();
}
