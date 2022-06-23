package _08_richFunction;

import org.apache.calcite.util.Static;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;
import sun.management.counter.Counter;

import java.util.Random;

/**
 * 1. 演示定时器
 * 2. 使用keyedprocessFunction实现POJO的reduce, 进而实现最大值, 最小值, 总数, 计数, 平均值
 * 3. 使用keyedprocessFunction实现Tuple5的最值
 */
public class _06_KeyedProcessFunction {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        DataStreamSource<Integer> integerDataStreamSource = executionEnvironment.addSource(new SourcePojo());
    
          
        
        
        
        executionEnvironment.execute();
    }
    
    // POJO
    public static class Statics{
        public Long ts;
        public Integer max;
        public Integer min;
        public Integer count;
        public Integer sum;
        public Integer avg; // POJO 就是做最简单的定义, 不做计算.
    
        @Override
        public String toString() {
            return "Static{" +
                     "ts=" + ts +
                     ", max=" + max +
                     ", min=" + min +
                     ", count=" + count +
                     ", sum=" + sum +
                     ", avg=" + avg +
                     '}';
        }
    
        public Statics(Long ts, Integer max, Integer min, Integer count, Integer sum, Integer avg) {
            this.ts = ts;
            this.max = max;
            this.min = min;
            this.count = count;
            this.sum = sum;
            this.avg = avg;
        }
    
        public Statics() {
        }
    }
    
    // SourcePOJO
    public static class SourcePojo implements SourceFunction<Integer>{
        boolean isRunning =true;
        Random random = new Random();
        @Override
        public void run(SourceContext<Integer> ctx) throws Exception {
            while (isRunning) {
                ctx.collect(random.nextInt(10));

            }
            Thread.sleep(2000L);
        }
    
        @Override
        public void cancel() {
            isRunning =false;
        }
    }
}
