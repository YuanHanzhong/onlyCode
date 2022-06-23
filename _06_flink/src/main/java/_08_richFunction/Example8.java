package _08_richFunction;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;

public class Example8 {
    
    
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .socketTextStream("hadoop102", 9999)
          .keyBy(r -> r)
          .process(new KeyedProcessFunction<String, String, String>() {
              @Override
              public void processElement(String in, Context ctx, Collector<String> out) throws Exception {
                  // 当前机器时间
                  long currTs = ctx.timerService().currentProcessingTime();
                  // 30秒之后的时间戳
                  long thirtySeconds = currTs + 30 * 1000L;
                  // 60秒之后的时间戳
                  long sixtySeconds = currTs + 60 * 1000L;
                  // 注册定时器
                  ctx.timerService().registerProcessingTimeTimer(thirtySeconds);
                  ctx.timerService().registerProcessingTimeTimer(sixtySeconds);
                  
                  out.collect("key: " + ctx.getCurrentKey() + "数据：" + in + " 到达的时间是：" + new Timestamp(currTs) + "" +
                                "注册的第一个定时器的时间戳：" + new Timestamp(thirtySeconds) + ";" +
                                "注册的第二个定时器的时间戳：" + new Timestamp(sixtySeconds));
              }
              
              @Override
              public void onTimer(long timerTs, OnTimerContext ctx, Collector<String> out) throws Exception {
                  out.collect("key为：" + ctx.getCurrentKey() + "的定时器触发了，" +
                                "定时器的时间戳是：" + new Timestamp(timerTs) + ";" +
                                "定时器真正执行的时间戳是：" + new Timestamp(ctx.timerService().currentProcessingTime()));
              }
          })
          .print();
        
        env.execute();
    }
}
