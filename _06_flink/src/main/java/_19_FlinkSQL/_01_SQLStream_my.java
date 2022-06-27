package _19_FlinkSQL;

import com.atguigu.utils.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.time.Duration;
import java.time.ZoneId;

import static org.apache.flink.table.api.Expressions.$;

public class _01_SQLStream_my {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        SingleOutputStreamOperator<UserBehavior> stream =
          env
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
    
        EnvironmentSettings envSetting = EnvironmentSettings.newInstance().build();
        TableEnvironment tEnv = TableEnvironment.create(envSetting);
    
        // set to UTC time zone
        tEnv.getConfig().setLocalTimeZone(ZoneId.of("Asia/Shanghai"));
        
        // 获取表执行环境
        StreamTableEnvironment streamTableEnvironment = StreamTableEnvironment
                                                          .create(
                                                            env,
                                                            EnvironmentSettings.newInstance().inStreamingMode().build()
                                                          );
    


// set to Shanghai time zone
//        tEnv.getConfig().setLocalTimeZone(ZoneId.of("Asia/Shanghai"));

    
        // 将数据流转换成动态表
        Table table = streamTableEnvironment.fromDataStream(
          stream
          ,
          $("userId"), // 需要完全和POJO的字段对应上
          $("productId"),
          $("categoryId"),
          $("type"),
          $("ts").rowtime() // rowtime()会把时间戳转化为
        );
        
        
    
        // 将动态表转换成数据流
        DataStream<Row> result = streamTableEnvironment.toChangelogStream(table);
        
        // +I表示INSERT
        result
          //.map(new MapFunction<Row, Row>() {
          //    @Override
          //    public Row map(Row value) throws Exception {
          //        value.setField(4, 1); //Caused by: java.lang.NullPointerException
          //
          //        return null;
          //    }
          //})

          
          
          // 下面的可以正常取出时间
          .map(new MapFunction<Row, String>() {
              @Override
              public String map(Row value) throws Exception {
                  return value.getField(4).toString();
              }
          })
          
          .print();
        
        env.execute();
    }
}
