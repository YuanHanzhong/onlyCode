

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
需求: 滚动窗口是1天, 但是想要每1秒看一眼, 铺垫

测试:
    a 1
    a 1
    a 10
    a 1
    
 */
public class _01_Triger_My {
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
              public TriggerResult onElement(Tuple2<String, Long> element, long timestamp, TimeWindow window, TriggerContext ctx) throws Exception {
                  return TriggerResult.FIRE;
              }
    
              @Override
              public TriggerResult onProcessingTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
                  return null;
              }
    
              @Override
              public TriggerResult onEventTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
                  return TriggerResult.FIRE;
              }
    
              @Override
              public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
        
              }
          })
          .process(new ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow>() {
              @Override
              public void process(String s, Context context, Iterable<Tuple2<String, Long>> elements, Collector<String> out) throws Exception {
                  out.collect("key: "+s+"  " +
                                ""+context.window().getStart()+"~" +
                                context.window().getEnd()+"  " +
                                "数据量为: "+ elements.spliterator().getExactSizeIfKnown());
              }
          })
          .print();
        
        env.execute();
    }
}
