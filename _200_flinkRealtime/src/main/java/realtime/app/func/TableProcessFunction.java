package realtime.app.func;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import realtime.bean.TableProcess;
import realtime.common.GmallConfig;
import realtime.utils.DruidDSUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
        dataSource = DruidDSUtil.createDataSource();
    }
    
    public TableProcessFunction(MapStateDescriptor<String, TableProcess> mapStateDescriptor) {
        this.mapStateDescriptor = mapStateDescriptor;
    }
    
    //处理主流业务数据
    @Override
    public void processElement(JSONObject value, ReadOnlyContext ctx, Collector<JSONObject> out) throws Exception {
    
    }
    
    /*{"before":{"source_table":"test","sink_table":"dim_test","sink_columns":"id,tm_name","sink_pk":"id","sink_extend":null},
    "after":{"source_table":"test","sink_table":"dim_test123","sink_columns":"id,tm_name","sink_pk":"id","sink_extend":null},
    "source":{"version":"1.5.4.Final","connector":"mysql","name":"mysql_binlog_source","ts_ms":1656492479000,"snapshot":"false",
    "db":"gmall2022_config","sequence":null,"table":"table_process","server_id":1,"gtid":null,"file":"mysql-bin.000012",
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
        StringBuilder createSql = new StringBuilder("create table if not exists " + GmallConfig.PHOENIX_SCHEMA + "." + tableName + "(");
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
        
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            
            //创建连接
            conn = dataSource.getConnection();
            //创建数据库操作对象
            ps = conn.prepareStatement(createSql.toString());
            //执行sql语句
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("~~在phoenix中建表失败:" + createSql);
        } finally {
            //释放资源
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
}
