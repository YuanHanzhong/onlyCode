package com.atguigu.gmall.publisher.service.impl;

import com.atguigu.gmall.publisher.bean.TradeProvinceOrderAmount;
import com.atguigu.gmall.publisher.mapper.TradeStatsMapper;
import com.atguigu.gmall.publisher.service.TradeStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Author: Felix
 * Date: 2022/6/7
 * Desc: 交易主题统计service接口实现类
 */
@Service
public class TradeStatsServiceImpl implements TradeStatsService {

    @Autowired // 标注在属性上, 到IOC容器中找指定类型的实例, 并给其赋值
    private TradeStatsMapper tradeStatsMapper; // ASK

    @Override
    public Double getGMV(Integer date) {
        return tradeStatsMapper.selectGMV(date);
    }

    @Override
    public List<TradeProvinceOrderAmount> getProvinceOrderAmount(Integer date) {
        return tradeStatsMapper.selectProvinceOrderAmount(date);
    }
}
