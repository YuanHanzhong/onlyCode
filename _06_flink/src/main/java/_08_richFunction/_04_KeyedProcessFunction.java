package _08_richFunction;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;

public class _04_KeyedProcessFunction {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        
        DataStreamSource<String> sourceNet = executionEnvironment.socketTextStream("hadoop102", 9999); // nc -l 9999 来模拟产生数据;
        
        sourceNet
          .keyBy(r -> r)
          .process(new KeyedProcessFunction<String, String, String>() {
              // keyedProcessFunction 最强大
                       // 设置定时器
                       @Override
                       public void processElement(String value, Context ctx, Collector<String> out) throws Exception {
                           long currentTs = ctx.timerService().currentProcessingTime();
                  
                           long thirtyTs = currentTs + 3 * 1000L;
                  
                           long sixtyTs = currentTs + 6 * 1000L;
                  
                           ctx.timerService().registerProcessingTimeTimer(thirtyTs); // 注意注册的是谁 got
                           ctx.timerService().registerEventTimeTimer(sixtyTs);
                  
                           out.collect("key: " + ctx.getCurrentKey() + "\n" +
                                         "数据: " + value + "\n" +
                                         "到达时间" + new Timestamp(currentTs) + "\n" +
                                         "1 timer" + new Timestamp(thirtyTs) + "\n" +
                                         "2 timer" + new Timestamp(sixtyTs) + "\n"
                           );
                       }
                       // 执行定时器
              
                       @Override
                       public void onTimer(long timerTs, OnTimerContext ctx, Collector<String> out) throws Exception {
                           out.collect("key为：" + ctx.getCurrentKey() + "的定时器触发了，\n" +
                                         "定时器的时间戳是：" + new Timestamp(timerTs) + ";\n" +
                                         "定时器真正执行的时间戳是：" + new Timestamp(ctx.timerService().currentProcessingTime()));
                       }
              
              
                   }
          
          
          )
          .print();
        
        
        executionEnvironment.execute();
        
    }
    
}
