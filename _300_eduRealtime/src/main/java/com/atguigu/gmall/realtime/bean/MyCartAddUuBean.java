package com.atguigu.gmall.realtime.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

/**

 * Desc: 用户加购实体类
 */
@Data
@AllArgsConstructor
public class MyCartAddUuBean {
    // 窗口起始时间
    String stt;
    // 窗口闭合时间
    String edt;
    // 加购独立用户数
    Long cartAddUuCt;
    // 时间戳
    Long ts;
}
