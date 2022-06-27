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

import java.time.Duration;
import java.time.ZoneId;

import static org.apache.flink.table.api.Expressions.$;

public class _02_SQLCount_my {
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
        
        // 获取表执行环境
        StreamTableEnvironment streamTableEnvironment = StreamTableEnvironment
                                                          .create(
                                                            env,
                                                            EnvironmentSettings.newInstance().inStreamingMode().build()
                                                          );
        // GOT  设置时区, 不管有没有设置, 输出结果都相同.
        streamTableEnvironment.getConfig().setLocalTimeZone(ZoneId.of("GMT+10"));
        
        // 将数据流转换成动态表
        Table table = streamTableEnvironment
                        .fromDataStream(
                          stream
                          // GOT 下面虽然可以注释掉, 但是要用sql的时候, 必须要指明, 不能用默认的
                          
                          ,
                          $("userId"),
                          $("productId"),
                          $("categoryId"),
                          $("type"),
                          // .rowtime表示这一列是事件时间
                          $("ts").rowtime()
                          
                          //$("ts").toTimestamp()
                          
                          
                          
                          
                        );
        
        // 将动态表注册为一个临时视图
        
        streamTableEnvironment.createTemporaryView("userbehavio", table); // 这里的pathSQL语句中的表名是对应的
        
        // 查询
        // HOP(时间戳的列名，滑动距离，窗口长度)
        // COUNT操作将窗口中的所有元素都收集起来然后数一遍
        // 相当于只使用ProcessWindowFunction的情况
        Table result = streamTableEnvironment
                         .sqlQuery(
                           "SELECT productId, COUNT(productId) as cnt," +
                             " HOP_START(ts, INTERVAL '5' MINUTES, INTERVAL '1' HOURS) as windowStartTime," +
                             " HOP_END(ts, INTERVAL '5' MINUTES, INTERVAL '1' HOURS) as windowEndTime" +
                             " FROM userbehavio GROUP BY" +
                             " productId, " +
                             " HOP(ts, INTERVAL '5' MINUTES, INTERVAL '1' HOURS)"
                         );
        // GOT sql 效率是最低的, 每来一条数据都要计算一次. 而自己定义的window则可以在一个window计算一次.
        
        streamTableEnvironment.toChangelogStream(result).print();
        
        env.execute();
    }
}
