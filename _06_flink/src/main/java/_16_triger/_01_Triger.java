

package _16_triger;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
/*
需求: 滚动窗口是1天, 但是想要每1秒看一眼

测试:
    a 1
    a 1
    a 10
    a 1
    
 */
public class _01_Triger {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .socketTextStream("hadoop102", 9999)
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
          .assignTimestampsAndWatermarks(
            WatermarkStrategy.<Tuple2<String, Long>>forMonotonousTimestamps()
              .withTimestampAssigner(new SerializableTimestampAssigner<Tuple2<String, Long>>() {
                  @Override
                  public long extractTimestamp(Tuple2<String, Long> element, long recordTimestamp) {
                      return element.f1;
                  }
              })
          )
          .keyBy(r -> r.f0)
          .window(TumblingEventTimeWindows.of(Time.seconds(10)))
          .trigger(new Trigger<Tuple2<String, Long>, TimeWindow>() {
              @Override
              public TriggerResult onElement(Tuple2<String, Long> in, long l, TimeWindow window, TriggerContext ctx) throws Exception {
                  // 每来一条数据，触发一次process算子的执行
                  return TriggerResult.FIRE;
              }
              
              @Override
              public TriggerResult onProcessingTime(long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
                  return null;
              }
              
              // 会在水位线到达窗口结束时间-1毫秒时默认触发调用
              @Override
              public TriggerResult onEventTime(long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
                  // 触发process算子的执行，然后删除窗口
                  return TriggerResult.FIRE_AND_PURGE;
              }
              
              @Override
              public void clear(TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
                  // 窗口闭合时调用，不需要实现
              }
          })
          .process(new ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow>() {
              @Override
              public void process(String key, Context ctx, Iterable<Tuple2<String, Long>> elements, Collector<String> out) throws Exception {
                  out.collect("key: " + key + ", " + ctx.window().getStart() + "~" + ctx.window().getEnd() + "" +
                                "有" + elements.spliterator().getExactSizeIfKnown() + "条数据");
              }
          })
          .print();
        
        env.execute();
    }
}
