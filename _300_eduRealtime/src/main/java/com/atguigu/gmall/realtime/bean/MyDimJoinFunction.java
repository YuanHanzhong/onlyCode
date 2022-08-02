package com.atguigu.gmall.realtime.bean;

import com.alibaba.fastjson.JSONObject;

/**
 * Desc: 维度关联需要实现的接口
 */
public interface MyDimJoinFunction<T> {
    void join(T obj, JSONObject dimInfoJsonObj);

    String getKey(T obj) ;
}
