package test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Felix
 * Date: 2022/5/24
 * Desc: 部门实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dept {
    private int deptno;
    private String dname;
    private Long ts;
}

// 2022/7/7 10:32 NOTE  如何实现双流join
/*
API
    interval
        connect
SQL
    内
    外
 */
