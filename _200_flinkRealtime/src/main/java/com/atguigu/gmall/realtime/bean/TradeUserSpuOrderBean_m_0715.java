package com.atguigu.gmall.realtime.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * Desc: 交易域用户-SPU粒度下单实体类
 */



@Data //data相关, get set
@AllArgsConstructor // 全参
@Builder //

public class TradeUserSpuOrderBean_m_0715 {
    // 窗口起始时间
    String stt;
    // 窗口结束时间
    String edt;
    
    //-------------------------维度
    // 品牌 ID
    String trademarkId;
    // 品牌名称
    String trademarkName;
    // 一级品类 ID
    String category1Id;
    // 一级品类名称
    String category1Name;
    // 二级品类 ID
    String category2Id;
    // 二级品类名称
    String category2Name;
    // 三级品类 ID
    String category3Id;
    // 三级品类名称
    String category3Name;
    // 订单 ID
    
    
    // set正好来去重,
    // 没有必要保存到clickhouse
    @TransientSink
    Set<String> orderIdSet;
    // sku_id
    @TransientSink
    String skuId;
    // 用户 ID
    String userId;
    // spu_id
    String spuId;
    // spu 名称
    String spuName;
    
    //------------------------------度量
    // 下单次数
    @Builder.Default
    Integer orderCount = 0;
    // 原始金额
    Double originalAmount;
    // 活动减免金额
    Double activityAmount;
    // 优惠券减免金额
    Double couponAmount;
    // 下单金额
    Double orderAmount ;
    // 时间戳
    Long ts;
}
