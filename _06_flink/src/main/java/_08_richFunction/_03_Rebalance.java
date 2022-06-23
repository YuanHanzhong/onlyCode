package _08_richFunction;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

public class _03_Rebalance {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
    
        executionEnvironment.addSource(new RichParallelSourceFunction<Integer>() {
            
            // 每一个并行子任务都会执行run
            
            @Override
            public void run(SourceContext<Integer> ctx) throws Exception {
                for (int i = 0; i < 9; i++) {
                    if (i % 3 == getRuntimeContext().getIndexOfThisSubtask()) {
                        
                        ctx.collect(i);
                    }
                }
            
            }
        
            @Override
            public void cancel() {
            
            }
        })
          .setParallelism(3)
          .rescale()
          .print()
          .setParallelism(6);
        
        executionEnvironment.execute();
    }
    
}
