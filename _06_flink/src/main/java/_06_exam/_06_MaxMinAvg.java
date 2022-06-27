package _06_exam;


import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.util.Random;

public class _06_MaxMinAvg {
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
        // 声明一个值状态变量
        private ValueState<IntStatistic> accumulator;
        
        @Override
        public void open(Configuration parameters) throws Exception {
            // 初始化值状态变量
            accumulator = getRuntimeContext().getState(
              new ValueStateDescriptor<IntStatistic>(
                // 针对每个状态变量取一个唯一的名字
                "accumulator",
                Types.POJO(IntStatistic.class)
              )
            );
        }
        
        @Override
        public void processElement(Integer in, Context ctx, Collector<IntStatistic> out) throws Exception {
            // accumulator.value() == null说明到达的数据是数据流的第一条数据
            // accumulator.value()获取的是输入数据in的key所对应的值状态变量中的值
            if (accumulator.value() == null) {
                // accumulator.update更新的是输入数据in所对应的状态变量
                accumulator.update(new IntStatistic(
                  in,
                  in,
                  in,
                  1,
                  in
                ));
            }
            // 后面的数据到达
            else {
                IntStatistic oldAcc = accumulator.value();
                // 输入数据和旧的累加器进行聚合
                IntStatistic newAcc = new IntStatistic(
                  Math.min(in, oldAcc.min),
                  Math.max(in, oldAcc.max),
                  in + oldAcc.sum,
                  1 + oldAcc.count,
                  (in + oldAcc.sum) / (1 + oldAcc.count)
                );
                accumulator.update(newAcc);
            }
            
            out.collect(accumulator.value());
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
