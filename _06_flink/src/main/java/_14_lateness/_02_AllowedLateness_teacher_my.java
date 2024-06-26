package _14_lateness;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

public class _02_AllowedLateness_teacher_my {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        SingleOutputStreamOperator<String> result =
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
            // 窗口会等待迟到数据5秒钟
            .allowedLateness(Time.seconds(5))
            // 将迟到且所属窗口已经不存在的数据发送到侧输出流中
            .sideOutputLateData(new OutputTag<Tuple2<String, Long>>("late-event") {
            })
            .process(new ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow>() {
                @Override
                public void process(String key, Context ctx, Iterable<Tuple2<String, Long>> elements, Collector<String> out) throws Exception {
                    out.collect("key: " + key + ", 在窗口" +
                                  "" + ctx.window().getStart() + "~" +
                                  "" + ctx.window().getEnd() + "里面有 " +
                                  "" + elements.spliterator().getExactSizeIfKnown() + " 条数据。");
                }
            });
        
        result.print("主流");
        
        // 取出来, get
        result.getSideOutput(new OutputTag<Tuple2<String, Long>>("late-event") {
        }).print("侧输出流");
        
        env.execute();
    }
}
