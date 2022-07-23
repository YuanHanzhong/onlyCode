package com.atguigu.gmall.publisher.mapper;

import com.atguigu.gmall.publisher.bean.TradeProvinceOrderAmount;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Author: Felix
 * Date: 2022/6/7
 * Desc: 交易主题统计mapper接口
 */
public interface TradeStatsMapper {
    //获取某一天总交易额
    // 2022/7/17 10:46 NOTE @Insert @Delete @
    @Select("select sum(order_amount) order_amount from dws_trade_province_order_window where toYYYYMMDD(stt)=#{date}") // #{}
    Double selectGMV(Integer date);

    //获取某一天各个地区交易额
    @Select("select province_name,sum(order_amount) order_amount from dws_trade_province_order_window where toYYYYMMDD(stt)=#{date} group by province_id,province_name")
    List<TradeProvinceOrderAmount> selectProvinceOrderAmount(Integer date);
}
