package com.atguigu.gmall.publisher.controller;

import com.atguigu.gmall.publisher.bean.TrafficUvCt;
import com.atguigu.gmall.publisher.service.TrafficStatsService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: Felix
 * Date: 2022/6/7
 * Desc: 流量统计controller
 */
@RestController
@RequestMapping("/traffic")
public class TrafficStatsController {

    @Autowired
    private TrafficStatsService trafficStatsService;

    @RequestMapping("/uv")
    public String getChUvCt(
        @RequestParam(value = "date", defaultValue = "0") Integer date,
        @RequestParam(value = "limit", defaultValue = "20") Integer limit
    ) {
        if (date == 0) {
            date = now();
        }

        List<TrafficUvCt> trafficUvCtList = trafficStatsService.getChUvCt(date, limit);
        List chList = new ArrayList();
        List uvList = new ArrayList();
        for (TrafficUvCt trafficUvCt : trafficUvCtList) {
            chList.add(trafficUvCt.getCh());
            uvList.add(trafficUvCt.getUvCt());
        }


        String json = "{\"status\": 0,\"msg\": \"\",\"data\": {" +
            "\"categories\": [\"" + StringUtils.join(chList, "\",\"") + "\"],\"series\": [ " +
            "{\"name\": \"渠道独立访客数\",\"data\": ["+StringUtils.join(uvList,",")+"]}]}}";
        return json;
    }

    private Integer now() {
        String yyyyMMdd = DateFormatUtils.format(new Date(), "yyyyMMdd");
        return Integer.valueOf(yyyyMMdd);
    }
}
