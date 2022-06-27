package _19_FlinkSQL;

import com.atguigu.utils.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.time.Duration;

import static org.apache.flink.table.api.Expressions.$;

// 和day06的Example1对比
public class _03_SQLCountHot_my {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        SingleOutputStreamOperator<UserBehavior> stream = env
                                                            .readTextFile("D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\UserBehavior.csv")
                                                            .map(new MapFunction<String, UserBehavior>() {
                                                                @Override
                                                                public UserBehavior map(String in) throws Exception {
                                                                    String[] array = in.split(",");
                                                                    return new UserBehavior(
                                                                      array[0], array[1], array[2], array[3],
                                                                      Long.parseLong(array[4]) * 1000L
                                                                    );
                                                                }
                                                            })
                                                            .filter(r -> r.type.equals("pv"))
                                                            .assignTimestampsAndWatermarks(
                                                              WatermarkStrategy.<UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(0))
                                                                .withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                                                                    @Override
                                                                    public long extractTimestamp(UserBehavior element, long recordTimestamp) {
                                                                        return element.ts;
                                                                    }
                                                                })
                                                            );
        
        // 获取表执行环境
        StreamTableEnvironment streamTableEnvironment = StreamTableEnvironment
                                                          .create(
                                                            env,
                                                            EnvironmentSettings.newInstance().inStreamingMode().build()
                                                          );
        
        // 将数据流转换成动态表
        Table table = streamTableEnvironment
                        .fromDataStream(
                          stream
                          ,
                          $("userId"),
                          $("productId"),
                          $("categoryId"),
                          $("type"),
                          // .rowtime表示这一列是事件时间
                          $("ts").rowtime()
                        );
        
        // 将动态表注册为一个临时视图
        streamTableEnvironment.createTemporaryView("userbehavior", table);
        
        // 查询
        // HOP(时间戳的列名，滑动距离，窗口长度)
        // COUNT操作将窗口中的所有元素都收集起来然后数一遍
        // 相当于只使用ProcessWindowFunction的情况
        // 计算每个商品在每个窗口中的浏览次数
        String innerSQL =
  
          "SELECT productId, COUNT(productId) as cnt," +
            " HOP_START(ts, INTERVAL '5' MINUTES, INTERVAL '1' HOURS) as windowStartTime," +
            " HOP_END(ts, INTERVAL '5' MINUTES, INTERVAL '1' HOURS) as windowEndTime" +
            " FROM userbehavior GROUP BY" +
            " productId, " +
            " HOP(ts, INTERVAL '5' MINUTES, INTERVAL '1' HOURS)";
          
   
        
        String midSQL = "SELECT *," +
                          " ROW_NUMBER() OVER (PARTITION BY windowEndTime ORDER BY cnt DESC) as row_num" +
                          " FROM (" + innerSQL + ")";
        
        String outerSQL = "SELECT * FROM (" + midSQL + ") WHERE row_num <= 3";
        
        Table result = streamTableEnvironment.sqlQuery(outerSQL);
        
        streamTableEnvironment.toChangelogStream(result)
          .map(new MapFunction<Row, Row>() {
              @Override
              public Row map(Row value) throws Exception {
                  return value;
              }
          })
          
          
          .print();
        
        env.execute();
    }
}
