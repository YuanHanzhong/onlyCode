package _17_;

import com.atguigu.utils.UserBehavior;
import com.google.common.base.Charsets;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import org.apache.flink.shaded.guava18.com.google.common.hash.BloomFilter;
import org.apache.flink.shaded.guava18.com.google.common.hash.Funnels;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.time.Duration;

/*
需求:
    独立访客, 按照用户ID去重
分析:
    用hashset实现, 只适合小的数据量, 精确去重
    使用布隆过滤器, 底层用bit数组实现, 可以确定一定不存在, 有点误判. 用来去重.

 */

public class _02_UvBloomFilter_my {
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
        
        stream
          .keyBy(r -> "user-behavior")
          .window(TumblingEventTimeWindows.of(Time.hours(1)))
          .aggregate(new CountAgg(), new WindowResult())
          .print();
        
        env.execute();
    }
    
    // 布隆过滤器只能告诉我们一个userId一定没来过，或者可能来过
    // 无法告诉我们有多少userId
    // Tuple2<布隆过滤器，一定没来过的userId的总数的统计值>
    public static class CountAgg implements AggregateFunction<UserBehavior, Tuple2<BloomFilter<String>, Long>, Long> {
        @Override
        public Tuple2<BloomFilter<String>, Long> createAccumulator() {
            return Tuple2.of(
              BloomFilter.create(
                Funnels.stringFunnel(Charsets.UTF_8), // 待去重类型是字符串
                100000,                               // 估算的待去重的数据量
                0.01                                  // 误判率
              ),
              0L // 统计值的初始值
            );
        }
        
        @Override
        public Tuple2<BloomFilter<String>, Long> add(UserBehavior in, Tuple2<BloomFilter<String>, Long> accumulator) {
            // 如果in.userId之前一定没来过
            if (!accumulator.f0.mightContain(in.userId)) {
                // 将相关bit位置为1
                accumulator.f0.put(in.userId);
                // 统计数据加1
                accumulator.f1 += 1L;
            }
            return accumulator;
        }
        
        @Override
        public Long getResult(Tuple2<BloomFilter<String>, Long> accumulator) {
            return accumulator.f1;
        }
        
        @Override
        public Tuple2<BloomFilter<String>, Long> merge(Tuple2<BloomFilter<String>, Long> a, Tuple2<BloomFilter<String>, Long> b) {
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
