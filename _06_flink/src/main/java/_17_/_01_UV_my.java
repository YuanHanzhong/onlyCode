package _17_;

import com.atguigu.utils.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.HashSet;

/*
需求:
    独立访客, 按照用户ID去重
分析:
    用hashset实现, 只适合小的数据量, 精确去重

 */
// 独立访客
public class _01_UV_my {
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
        
        stream
          .keyBy(r -> "user-behavior")
          .window(TumblingEventTimeWindows.of(Time.hours(1)))
          .aggregate(new CountAgg(), new WindowResult())
          .print();
        
        env.execute();
    }
    
    public static class CountAgg implements AggregateFunction<UserBehavior, HashSet<String>, Long> {
        @Override
        public HashSet<String> createAccumulator() {
            return new HashSet<>();
        }
        
        @Override
        public HashSet<String> add(UserBehavior in, HashSet<String> accumulator) {
            // 如果in.userId在hashset中已经存在，那么add将不起作用
            // 每个in.userId只会在hashset中添加一次，幂等性
            accumulator.add(in.userId);
            return accumulator;
        }
        
        @Override
        public Long getResult(HashSet<String> accumulator) {
            // 每个窗口中的独立访客数量就是hashset中的元素数量
            return (long) accumulator.size();
        }
        
        @Override
        public HashSet<String> merge(HashSet<String> a, HashSet<String> b) {
            return null;
        }
    }
    
    public static class WindowResult extends ProcessWindowFunction<Long, String, String, TimeWindow> {
        @Override
        public void process(String key, Context ctx, Iterable<Long> elements, Collector<String> out) throws Exception {
            out.collect("窗口：" + new Timestamp(ctx.window().getStart()) + "~" +
                          "" + new Timestamp(ctx.window().getEnd()) + "的uv是：" +
                          "" + elements.iterator().next());
        }
    }
}
