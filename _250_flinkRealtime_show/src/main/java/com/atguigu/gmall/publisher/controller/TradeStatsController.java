package com.atguigu.gmall.publisher.controller;

import com.atguigu.gmall.publisher.bean.TradeProvinceOrderAmount;
import com.atguigu.gmall.publisher.service.TradeStatsService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * Author: Felix
 * Date: 2022/6/7
 * Desc: 交易主题表示层controller
 */
@RestController
@RequestMapping("/trade") // 2022/7/17 13:13 NOTE 业务范围, 相当于命名空间
public class TradeStatsController {

    @Autowired // 2022/7/17 11:19 NOTE 自动赋值
    private TradeStatsService tradeStatsService;

    @RequestMapping("/gmv")
    public String getGMV(@RequestParam(value = "date", defaultValue = "0") Integer date) {
        //判断请求中是否传递日期参数
        if (date == 0) {
            date = now();
        }
        //调用service方法获取总交易额
        Double gmv = tradeStatsService.getGMV(date);
        String json = "{\"status\": 0,\"data\": " + gmv + "}";
        return json;
    }

    @RequestMapping("/province")
    public String getProvinceOrderAmount(@RequestParam(value = "date",defaultValue = "0") Integer date){
        if(date == 0){
            date = now();
        }
        List<TradeProvinceOrderAmount> tradeProvinceOrderAmountList = tradeStatsService.getProvinceOrderAmount(date);
        StringBuilder jsonB = new StringBuilder("{\"status\": 0,\"data\": {\"mapData\": [");
        for (int i = 0; i < tradeProvinceOrderAmountList.size(); i++) {
            TradeProvinceOrderAmount tradeProvinceOrderAmount = tradeProvinceOrderAmountList.get(i);
            jsonB.append("{\"name\": \""+tradeProvinceOrderAmount.getProvinceName()+"\",\"value\": "+tradeProvinceOrderAmount.getOrderAmount()+"}");
            if(i < tradeProvinceOrderAmountList.size() - 1){
                jsonB.append(",");
            }
        }
        jsonB.append(" ], \"valueName\": \"总交易额\"}}");
        return jsonB.toString();
    }

    private Integer now() {
        String yyyyMMdd = DateFormatUtils.format(new Date(), "yyyyMMdd");
        return Integer.valueOf(yyyyMMdd);
    }
}
