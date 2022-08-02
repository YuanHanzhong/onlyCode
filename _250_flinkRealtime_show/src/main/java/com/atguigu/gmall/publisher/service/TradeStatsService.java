package com.atguigu.gmall.publisher.service;

import com.atguigu.gmall.publisher.bean.TradeProvinceOrderAmount;

import java.util.List;

/**
 * Author: Felix
 * Date: 2022/6/7
 * Desc: 交易主题统计Service接口
 */
public interface TradeStatsService {
    //获取某一天总交易额
    Double getGMV(Integer date);

    List<TradeProvinceOrderAmount> getProvinceOrderAmount(Integer date);
}
