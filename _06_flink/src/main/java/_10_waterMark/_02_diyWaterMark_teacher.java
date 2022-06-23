package _10_waterMark;

import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class _02_diyWaterMark_teacher {
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
            new WatermarkStrategy<Tuple2<String, Long>>() {
                @Override
                public TimestampAssigner<Tuple2<String, Long>> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
                    return new SerializableTimestampAssigner<Tuple2<String, Long>>() {
                        @Override
                        public long extractTimestamp(Tuple2<String, Long> element, long recordTimestamp) {
                            return element.f1; // 指定事件时间戳是哪一个字段
                        }
                    };
                }
                
                @Override
                public WatermarkGenerator<Tuple2<String, Long>> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
                    return new WatermarkGenerator<Tuple2<String, Long>>() {
                        private long delay = 5000L; // 最大延迟时间是5000ms
                        // 为了防止溢出，初始值加上(delay+1L)
                        private long maxTs = Long.MIN_VALUE + delay + 1L; // maxTs用来保存观察到的最大时间戳
                        
                        @Override
                        public void onEvent(Tuple2<String, Long> event, long eventTimestamp, WatermarkOutput output) {
                            // `onEvent`每来一条数据触发一次调用
                            // 更新观察到的最大时间戳
                            // 比较输入数据的时间戳和maxTs的大小，选择大的更新maxTs
                            maxTs = Math.max(event.f1, maxTs);
                            
                            // 向下游发送水位线事件
                            if (event.f0.equals("hello")) {
                                output.emitWatermark(new Watermark(Long.MAX_VALUE));
                            }
                        }
                        
                        @Override
                        public void onPeriodicEmit(WatermarkOutput output) {
                            // `onPeriodicEmit`默认每隔200ms触发一次执行
                            output.emitWatermark(new Watermark(
                              maxTs - delay - 1L
                            ));
                        }
                    };
                }
            }
          )
          .keyBy(r -> r.f0)
          .window(TumblingEventTimeWindows.of(Time.seconds(10)))
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
