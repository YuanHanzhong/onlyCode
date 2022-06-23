package _08_richFunction;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;


  
  import org.apache.flink.api.common.state.ValueState;
  import org.apache.flink.api.common.state.ValueStateDescriptor;
  import org.apache.flink.api.common.typeinfo.Types;
  import org.apache.flink.configuration.Configuration;
  import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
  import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
  import org.apache.flink.streaming.api.functions.source.SourceFunction;
  import org.apache.flink.util.Collector;
  
  import java.util.Random;

public class _08_Spark_Integer_teacher {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new IntSource())
          .keyBy(r -> "number")
          .process(new Statistics())
          .print();
        
        env.execute();
    }
    
    public static class Statistics extends KeyedProcessFunction<String, Integer, IntStatistic> {
        private ValueState<IntStatistic> acc;
        // 如果flag为空，说明此时不存在定时器
        // 如果flag不为空，说明此时存在定时器
        private ValueState<Integer> flag;
        @Override
        public void open(Configuration parameters) throws Exception {
            acc = getRuntimeContext().getState(
              new ValueStateDescriptor<IntStatistic>(
                "acc",
                Types.POJO(IntStatistic.class)
              )
            );
            flag = getRuntimeContext().getState(
              new ValueStateDescriptor<Integer>(
                "flag",
                Types.INT
              )
            );
        }
        
        @Override
        public void processElement(Integer in, Context ctx, Collector<IntStatistic> out) throws Exception {
            if (acc.value() == null) {
                acc.update(new IntStatistic(
                  in, in, in, 1, in
                ));
            } else {
                IntStatistic oldAcc = acc.value();
                IntStatistic newAcc = new IntStatistic(
                  Math.min(in, oldAcc.min),
                  Math.max(in, oldAcc.max),
                  in + oldAcc.sum,
                  1 + oldAcc.count,
                  (in + oldAcc.sum) / (1 + oldAcc.count)
                );
                acc.update(newAcc);
            }
            
            // 只在不存在定时器的情况下才注册定时器
            if (flag.value() == null) {
                ctx.timerService().registerProcessingTimeTimer(
                  ctx.timerService().currentProcessingTime() + 10 * 1000L
                );
                // 将标志位置为非空
                flag.update(1);
            }
        }
        
        // 定时器用来向下游发送统计结果
        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<IntStatistic> out) throws Exception {
            out.collect(acc.value());
            // 发送完统计结果之后，将flag置为空
            // 这样就能继续注册定时器了
            flag.clear();
        }
    }
    
    public static class IntStatistic {
        public Integer min;
        public Integer max;
        public Integer sum;
        public Integer count;
        public Integer avg;
        
        public IntStatistic() {
        }
        
        public IntStatistic(Integer min, Integer max, Integer sum, Integer count, Integer avg) {
            this.min = min;
            this.max = max;
            this.sum = sum;
            this.count = count;
            this.avg = avg;
        }
        
        @Override
        public String toString() {
            return "(最小值: " + min + ", 最大值: " + max + ", " +
                     "总和: " + sum + ", 总条数: " + count + ", " +
                     "平均值: " + avg + ")";
        }
    }
    
    public static class IntSource implements SourceFunction<Integer> {
        private boolean running = true;
        private Random random = new Random();
        @Override
        public void run(SourceContext<Integer> ctx) throws Exception {
            while (running) {
                ctx.collect(random.nextInt(1000));
                Thread.sleep(1000L);
            }
        }
        
        @Override
        public void cancel() {
            running = false;
        }
    }
}
