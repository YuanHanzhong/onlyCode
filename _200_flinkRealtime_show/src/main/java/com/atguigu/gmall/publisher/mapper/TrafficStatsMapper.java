package com.atguigu.gmall.publisher.mapper;

import com.atguigu.gmall.publisher.bean.TrafficUvCt;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Author: Felix
 * Date: 2022/6/7
 * Desc: 流量主题统计Mapper接口
 */
// 2022/7/17 14:38 NOTE P1 这里可以优化

public interface TrafficStatsMapper {
    @Select("select ch,sum(uv_ct) uv_ct from dws_traffic_vc_ch_ar_is_new_page_view_window where toYYYYMMDD(stt)=#{date} group by ch order by uv_ct desc limit #{limit}")
    List<TrafficUvCt> selectChUvCt(@Param("date") Integer date, @Param("limit") Integer limit);
}
