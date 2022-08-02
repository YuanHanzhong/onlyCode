package com.atguigu.gmall.publisher.service;

import com.atguigu.gmall.publisher.bean.TrafficUvCt;

import java.util.List;

/**
 * Author: Felix
 * Date: 2022/6/7
 * Desc: 流量统计service接口
 */
public interface TrafficStatsService {
    List<TrafficUvCt> getChUvCt(Integer date, Integer limit);
}
