package com.atguigu.gmall.realtime.app.func;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.bean.TableProcess;
import com.atguigu.gmall.realtime.common.MyGmallConfig;
import com.atguigu.gmall.realtime.util.MyDruidDSUtil;
import com.atguigu.gmall.realtime.util.MyPhoenixUtil;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: Felix
 * Date: 2022/7/1
 * Desc: 从主流业务数据中过滤出维度数据
 */
public class TableProcessFunction extends BroadcastProcessFunction<JSONObject, String, JSONObject> {

    private MapStateDescriptor<String, TableProcess> mapStateDescriptor;

    private DruidDataSource dataSource;

    @Override
    public void open(Configuration parameters) throws Exception {
        dataSource = MyDruidDSUtil.createDataSource();
    }

    public TableProcessFunction(MapStateDescriptor<String, TableProcess> mapStateDescriptor) {
        this.mapStateDescriptor = mapStateDescriptor;
    }

    //{"database":"education","xid":17734,"data":{"tm_name":"cls","logo_url":"fds","id":12},
    // "commit":true,"type":"insert","table":"base_trademark","ts":1656656361}
    //处理主流业务数据
    @Override
    public void processElement(JSONObject jsonObj, ReadOnlyContext ctx, Collector<JSONObject> out) throws Exception {
        //获取广播状态
        ReadOnlyBroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
        //获取当前处理的业务数据表名
        String tableName = jsonObj.getString("table");
        //根据表名  到广播状态中查询对应的配置信息     如果能查询到对应的配置，说明是维度，继续向下游传递；
        //如果在广播状态中，没有找到对应的配置，说明不是维度，不做任何处理，相当于将非维度数据过滤掉了
        TableProcess tableProcess = broadcastState.get(tableName);
        if (tableProcess != null) {
            //是维度 继续向下游传递   只需要将影响的维度表中的当前这一条记录向下游传递即可
            //{"tm_name":"cls","logo_url":"fds","id":12}
            JSONObject dataJsonObj = jsonObj.getJSONObject("data");

            //将不需要保留的维度属性过滤掉
            //{"tm_name":"cls","id":12}
            String sinkColumns = tableProcess.getSinkColumns();
            filterColumn(dataJsonObj, sinkColumns);

            //根据配置对象，获取向phoenix的哪张表中保存这条数据
            String sinkTable = tableProcess.getSinkTable();
            //在向下游传递之前，应该给传递的json补充一个属性，就是最终输出的目的地
            //{"tm_name":"cls","id":12,"sink_table":"dim_base_trademark"}
            dataJsonObj.put("sink_table", sinkTable);

            //补充操作类型
            dataJsonObj.put("type",jsonObj.getString("type"));
            out.collect(dataJsonObj);
        }

    }

    //过滤掉不需要向下游传递的属性
    // dataJsonObj： {"tm_name":"cls","logo_url":"fds","id":12}
    // sinkColumns:  id,tm_name
    private void filterColumn(JSONObject dataJsonObj, String sinkColumns) {
        String[] columnArr = sinkColumns.split(",");
        List<String> columnList = Arrays.asList(columnArr);

        Set<Map.Entry<String, Object>> entrySet = dataJsonObj.entrySet();
        entrySet.removeIf(entry -> !columnList.contains(entry.getKey()));
    }

    /*{"before":{"source_table":"test","sink_table":"dim_test","sink_columns":"id,tm_name","sink_pk":"id","sink_extend":null},
    "after":{"source_table":"test","sink_table":"dim_test123","sink_columns":"id,tm_name","sink_pk":"id","sink_extend":null},
    "source":{"version":"1.5.4.Final","connector":"mysql","name":"mysql_binlog_source","ts_ms":1656492479000,"snapshot":"false",
    "db":"education_config","sequence":null,"table":"table_process","server_id":1,"gtid":null,"file":"mysql-bin.000012",
    "pos":1054519,"row":0,"thread":null,"query":null},"op":"u","ts_ms":1656492477398,"transaction":null}*/
    //处理广播流配置数据
    @Override
    public void processBroadcastElement(String jsonStr, Context ctx, Collector<JSONObject> out) throws Exception {
        //为了处理方便，将flinkCDC采集的数据 封装为json对象
        JSONObject jsonObj = JSON.parseObject(jsonStr);

        //获取广播状态
        BroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(mapStateDescriptor);

        //获取对配置表的操作类型
        String op = jsonObj.getString("op");
        if ("d".equals(op)) {
            //如果对配置表进行的是删除操作，将对应的配置信息从广播状态中删除处理
            TableProcess before = jsonObj.getObject("before", TableProcess.class);
            String sourceTable = before.getSourceTable();
            broadcastState.remove(sourceTable);
        } else {
            //如果对配置表进行的是添加或者是修改操作，键对应的配置信息添加到广播状态或者对广播状态中的配置进行修改
            TableProcess after = jsonObj.getObject("after", TableProcess.class);
            //获取业务数据库表名
            String sourceTable = after.getSourceTable();
            //获取数仓中输出的目的地表名
            String sinkTable = after.getSinkTable();
            //获取数仓中对应的表中的字段
            String sinkColumns = after.getSinkColumns();
            //获取数仓中对应的表的主键
            String sinkPk = after.getSinkPk();
            //获取建表扩展
            String sinkExtend = after.getSinkExtend();

            //根据配置表中的配置 提前将维度表创建出来
            checkTable(sinkTable, sinkColumns, sinkPk, sinkExtend);

            //将添加或者修改后的配置放到广播状态中
            broadcastState.put(sourceTable, after);
        }

    }

    //维度表创建
    private void checkTable(String tableName, String sinkColumns, String pk, String ext) {
        //空值处理
        if (pk == null) {
            pk = "id";
        }

        if (ext == null) {
            ext = "";
        }
        //拼接建表语句
        StringBuilder createSql = new StringBuilder("create table if not exists " + MyGmallConfig.PHOENIX_SCHEMA + "." + tableName + "(");
        //表中字段的处理
        String[] columnArr = sinkColumns.split(",");
        for (int i = 0; i < columnArr.length; i++) {
            String column = columnArr[i];
            //判断当前字段是否为主键字段
            if (column.equals(pk)) {
                createSql.append(column + " varchar primary key");
            } else {
                createSql.append(column + " varchar");
            }
            //除了最后一个字段之外，其它字段后面都要加逗号分割
            if (i < columnArr.length - 1) {
                createSql.append(",");
            }
        }

        createSql.append(")" + ext);
        System.out.println("在phoenix中建表的sql：" + createSql);

        try {
            //创建连接
            Connection conn = dataSource.getConnection();
            //执行建表语句
            MyPhoenixUtil.executeSql(createSql.toString(),conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
