package com.atguigu.gmall.realtime.app.func;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.common.GmallConfig;
import com.atguigu.gmall.realtime.util.DimUtil;
import com.atguigu.gmall.realtime.util.DruidDSUtil;
import com.atguigu.gmall.realtime.util.PhoenixUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;

/**
 * Author: Felix
 * Date: 2022/7/1
 * Desc: 将流中的维度数据写到phoenix表中
 */
public class DimSinkFunction extends RichSinkFunction<JSONObject> {
    private DruidDataSource dataSource;

    @Override
    public void open(Configuration parameters) throws Exception {
        dataSource = DruidDSUtil.createDataSource();
    }

    //{"tm_name":"cls","sink_table":"dim_base_trademark","id":12}
    @Override
    public void invoke(JSONObject jsonObj, Context context) throws Exception {
        //获取输出的目的地表名
        String tableName = jsonObj.getString("sink_table");
        jsonObj.remove("sink_table");

        String type = jsonObj.getString("type");
        jsonObj.remove("type");

        //{"tm_name":"cls","id":12}
        //拼接向phoenix表中插入数据的语句
        String upsertSql = "upsert into " + GmallConfig.PHOENIX_SCHEMA + "." + tableName
            + " (" + StringUtils.join(jsonObj.keySet(), ",") + ") " +
            " values('" + StringUtils.join(jsonObj.values(), "','") + "')";
        System.out.println("向phoenix表中插入数据的sql:" + upsertSql);

        //获取连接
        Connection conn = dataSource.getConnection();
        PhoenixUtil.executeSql(upsertSql,conn);

        //如果维度数据发生了修改操作，需要将缓存中的维度删除掉
        if("update".equals(type)){
            DimUtil.delCached(tableName,jsonObj.getString("id"));
        }
    }
}
