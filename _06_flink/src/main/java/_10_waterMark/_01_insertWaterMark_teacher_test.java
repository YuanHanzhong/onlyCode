package _10_waterMark;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

public class _01_insertWaterMark_teacher_test {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new SourceFunction<Integer>() {
              @Override
              public void run(SourceContext<Integer> ctx) throws Exception {
                  // 第一个参数：要发送的事件
                  // 第二个参数：事件时间
                  ctx.collectWithTimestamp(1, 1000L);
              }
              
              @Override
              public void cancel() {
              
              }
          })
          .keyBy(r -> "number")
          .process(new KeyedProcessFunction<String, Integer, String>() {
              @Override
              public void processElement(Integer in, Context ctx, Collector<String> out) throws Exception {
                  // ctx.timestamp()获取输入数据的事件时间
                  ctx.timerService().registerEventTimeTimer(
                    ctx.timestamp() + 5000L
                  );
                  out.collect("数据：" + in + " 到达，当前KeyedProcessFunction的并行子任务的水位线是：" +
                                "" + ctx.timerService().currentWatermark());
              }
              
              @Override
              public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                  out.collect("时间戳是 " + timestamp + " 的定时器触发了，" +
                                "当前KeyedProcessFunction的并行子任务的水位线是：" + ctx.timerService().currentWatermark());
              }
          })
          .print();
        
        env.execute();
    }
}
