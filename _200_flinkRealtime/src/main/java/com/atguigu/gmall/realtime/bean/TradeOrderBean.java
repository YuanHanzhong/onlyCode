package com.atguigu.gmall.realtime.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Author: Felix
 * Date: 2022/6/1
 * Desc: 交易域下单实体类
 */
@Data
@AllArgsConstructor
@Builder
public class TradeOrderBean {
    // 窗口起始时间
    String stt;
    // 窗口关闭时间
    String edt;
    // 下单独立用户数
    Long orderUniqueUserCount;
    // 下单新用户数
    Long orderNewUserCount;
    // 时间戳
    Long ts;
}
