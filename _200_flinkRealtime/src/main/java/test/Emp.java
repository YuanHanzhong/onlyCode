package test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Felix
 * Date: 2022/5/24
 * Desc: 员工实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Emp {
    private int empno;
    private String ename;
    private int deptno;
    private Long ts;
}
