package _12_unionWaterMark;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

public class _01_unionWaterMark_teacher {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        SingleOutputStreamOperator<Tuple2<String, Long>> stream1 =
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
              WatermarkStrategy.<Tuple2<String, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(0))
                .withTimestampAssigner(new SerializableTimestampAssigner<Tuple2<String, Long>>() {
                    @Override
                    public long extractTimestamp(Tuple2<String, Long> element, long recordTimestamp) {
                        return element.f1;
                    }
                })
            );
        
        SingleOutputStreamOperator<Tuple2<String, Long>> stream2 =
          env
            .socketTextStream("hadoop102", 9998)
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
              WatermarkStrategy.<Tuple2<String, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(0))
                .withTimestampAssigner(new SerializableTimestampAssigner<Tuple2<String, Long>>() {
                    @Override
                    public long extractTimestamp(Tuple2<String, Long> element, long recordTimestamp) {
                        return element.f1;
                    }
                })
            );
        
        stream1
          .union(stream2)
          .process(new ProcessFunction<Tuple2<String, Long>, String>() {
              @Override
              public void processElement(Tuple2<String, Long> in, Context ctx, Collector<String> out) throws Exception {
                  out.collect("输入数据：" + in + "，当前process算子的水位线是：" +
                                "" + ctx.timerService().currentWatermark());
              }
          })
          .print();
        
        env.execute();
    }
}
