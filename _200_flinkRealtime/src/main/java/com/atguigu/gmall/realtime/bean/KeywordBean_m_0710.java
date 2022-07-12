package com.atguigu.gmall.realtime.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Felix
 * Date: 2022/5/28
 * Desc: 关键词统计实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeywordBean_m_0710 {
    // 窗口起始时间
    private String stt;
    // 窗口闭合时间
    private String edt;
    // 关键词来源
    // 2022/7/10 15:11 NOTE transant 修饰后就不再序列化和反序列化了
    // 2022/7/10 15:12 NOTE 自定义注解
    @TransientSink
    private String source;
    // 关键词
    private String keyword;
    // 关键词出现频次
    private Long keyword_count;
    // 时间戳
    private Long ts;
}
