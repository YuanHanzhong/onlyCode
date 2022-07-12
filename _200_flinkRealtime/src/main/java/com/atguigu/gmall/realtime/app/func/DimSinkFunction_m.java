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
 * Date: 2022/5/20
 * Desc: 将维度流中的数据 写到phoenix表中
 */
public class DimSinkFunction_m extends RichSinkFunction<JSONObject> {

    private DruidDataSource druidDataSource;

    @Override
    public void open(Configuration parameters) throws Exception {
        druidDataSource = DruidDSUtil.createDataSource();
    }

    //将流中的数据，保存到phoenix不同的维度表中
    // jonObj:  {"tm_name":"xzls11","sink_table":"dim_base_trademark","id":12}
    @Override
    public void invoke(JSONObject jsonObj, Context context) throws Exception {

        //获取维度输出的目的地表名
        String tableName = jsonObj.getString("sink_table");
        //为了将jsonObj中的所有属性保存到phoenix表中，需要将输出目的地从jsonObj中删除掉
        //===>{"tm_name":"xzls11","id":12}
        jsonObj.remove("sink_table");

        String type = jsonObj.getString("type");
        jsonObj.remove("type");

        String upsertSQL = "upsert into " + GmallConfig.PHOENIX_SCHEMA + "." + tableName
            + "(" + StringUtils.join(jsonObj.keySet(), ",") + ") " +
            " values" +
            " ('" + StringUtils.join(jsonObj.values(), "','") + "')";
        System.out.println("向phoenix表中插入数据的语句为：" + upsertSQL);

        //从连接池中获取连接对象
        Connection conn =  druidDataSource.getConnection();
        //调用向Phoenix表中插入数据的方法
        PhoenixUtil.executeSQL(upsertSQL,conn);

        //如果维度数据发生了upsert,清除Redis中缓存的维度数据
        if("update".equals(type)){
            DimUtil.deleteCached(tableName,jsonObj.getString("id"));
        }

    }
}
