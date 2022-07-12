package com.atguigu.gmall.realtime.app.func;

import com.alibaba.fastjson.JSONObject;

/**
 * Author: Felix
 * Date: 2022/6/6
 * Desc: 维度关联需要实现的接口
 */
public interface DimJoinFunction<T> {
    void join(T obj, JSONObject dimInfoJsonObj);

    String getKey(T obj);
}
