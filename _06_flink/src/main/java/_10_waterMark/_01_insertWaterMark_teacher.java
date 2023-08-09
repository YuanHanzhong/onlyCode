package _10_waterMark;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;
/*
测试触发
水位线只能越发越大, 发小的没有


发送水位线, 可以在很多地方
    1. 自定义数据源里
    2.
 */

public class _01_insertWaterMark_teacher {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          // a 1
          // a 2
          .socketTextStream("hadoop102", 9999)
          // "a 1" -> ("a", 1000L)
          .map(new MapFunction<String, Tuple2<String, Long>>() {
              @Override
              public Tuple2<String, Long> map(String in) throws Exception {
                  String[] array = in.split(" ");
                  return Tuple2.of(
                    array[0],
                    Long.parseLong(array[1]) * 1000L
                  );
              }
          })
          
          
          // 在map算子输出的数据流中插入水位线事件
          // 默认每隔200ms插入一次水位线事件
          .assignTimestampsAndWatermarks(
            // 设置最大延迟时间为5秒钟:`Duration.ofSeconds(5)`
            WatermarkStrategy.<Tuple2<String, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(5))
              .withTimestampAssigner(new SerializableTimestampAssigner<Tuple2<String, Long>>() {
                  @Override
                  public long extractTimestamp(Tuple2<String, Long> element, long recordTimestamp) {
                      return element.f1; // 指定哪一个字段是事件时间戳
                  }
              })
          )
          
          
          .keyBy(r -> r.f0)
          .window(TumblingEventTimeWindows.of(Time.seconds(10)))
          //.window(TumblingEventTimeWindows.of(Duration.ofSeconds(10)))
          .process(new ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow>() {
              @Override
              public void process(String key, Context ctx, Iterable<Tuple2<String, Long>> elements, Collector<String> out) throws Exception {
                  out.collect("key: " + key + ", 在窗口" +
                                "" + ctx.window().getStart() + "~" +
                                "" + ctx.window().getEnd() + "里面有 " +
                                "" + elements.spliterator().getExactSizeIfKnown() + " 条数据。");
              }
          })
          .print();
        
        env.execute();
    }
}
