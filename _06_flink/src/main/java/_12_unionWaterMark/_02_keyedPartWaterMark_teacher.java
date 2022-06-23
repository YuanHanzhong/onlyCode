package _12_unionWaterMark;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;

import java.time.Duration;


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
  
  import java.sql.Timestamp;
  import java.time.Duration;

public class _02_keyedPartWaterMark_teacher {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        env
          .socketTextStream("hadoop102", 9999)
          .setParallelism(1)
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
          .setParallelism(1)
          .assignTimestampsAndWatermarks(
            WatermarkStrategy.<Tuple2<String, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(0))
              .withTimestampAssigner(new SerializableTimestampAssigner<Tuple2<String, Long>>() {
                  @Override
                  public long extractTimestamp(Tuple2<String, Long> element, long recordTimestamp) {
                      return element.f1;
                  }
              })
          )
          .keyBy(r -> r.f0)
          .window(TumblingEventTimeWindows.of(Time.seconds(5)))
          .process(new ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow>() {
              @Override
              public void process(String key, Context ctx, Iterable<Tuple2<String, Long>> elements, Collector<String> out) throws Exception {
                  out.collect("key: " + key + "," + new Timestamp(ctx.window().getStart()) + "~" +
                                "" + new Timestamp(ctx.window().getEnd()) + ",数据量为" +
                                "" + elements.spliterator().getExactSizeIfKnown() + ",并行子任务的索引为" +
                                "" + getRuntimeContext().getIndexOfThisSubtask());
              }
          })
          .setParallelism(4)
          .print()
          .setParallelism(4);
        
        env.execute();
    }
}
