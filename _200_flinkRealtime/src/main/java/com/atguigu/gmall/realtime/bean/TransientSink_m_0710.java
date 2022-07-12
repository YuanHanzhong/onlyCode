package com.atguigu.gmall.realtime.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: Felix
 * Date: 2022/5/30
 * // 2022/7/10 15:12 NOTE new 时选择annotation,
 * Desc: 自定义注解  用于标注不需要保存到ClickHouse的属性
 */
@Target(ElementType.FIELD) // 2022/7/10 15:14 NOTE 元注解, 注解的注解
@Retention(RetentionPolicy.RUNTIME) // 2022/7/10 15:14 NOTE 作用范围
// 2022/7/10 15:12 NOTE 和接口区别在于@
public @interface TransientSink_m_0710 {
}
