package _15_CoStream;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class _04_IntervalJoin {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        SingleOutputStreamOperator<Event> leftStream = env
                                                         .fromElements(
                                                           new Event("key-1", "left", 10 * 1000L)
                                                         )
                                                         .assignTimestampsAndWatermarks(
                                                           WatermarkStrategy.<Event>forMonotonousTimestamps()
                                                             .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                                                                 @Override
                                                                 public long extractTimestamp(Event element, long recordTimestamp) {
                                                                     return element.ts;
                                                                 }
                                                             })
                                                         );
        
        SingleOutputStreamOperator<Event> rightStream = env
                                                          .fromElements(
                                                            new Event("key-1", "right", 1000L),
                                                            new Event("key-1", "right", 6000L),
                                                            new Event("key-1", "right", 11 * 1000L),
                                                            new Event("key-1", "right", 16 * 1000L)
                                                          )
                                                          .assignTimestampsAndWatermarks(
                                                            WatermarkStrategy.<Event>forMonotonousTimestamps()
                                                              .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                                                                  @Override
                                                                  public long extractTimestamp(Event element, long recordTimestamp) {
                                                                      return element.ts;
                                                                  }
                                                              })
                                                          );
        
        leftStream.keyBy(r -> r.key)
          .intervalJoin(rightStream.keyBy(r -> r.key))
          .between(Time.seconds(-5), Time.seconds(5))
          .process(new ProcessJoinFunction<Event, Event, String>() {
              @Override
              public void processElement(Event left, Event right, Context ctx, Collector<String> out) throws Exception {
                  out.collect(left + " => " + right);
              }
          })
          .print();
        
        
        env.execute();
    }
    
    public static class Event {
        public String key;
        public String value;
        public Long ts;
        
        public Event() {
        }
        
        public Event(String key, String value, Long ts) {
            this.key = key;
            this.value = value;
            this.ts = ts;
        }
        
        @Override
        public String toString() {
            return "(" +
                     key +
                     "," + value +
                     "," + ts +
                     ")";
        }
    }
}
