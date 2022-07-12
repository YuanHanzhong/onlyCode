package _17_UV;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

public class _05_SaveCheckPoint
{
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env.enableCheckpointing(60 * 1000L);
        
        env
          .addSource(new CounterSource())
          .print();
        
        env.execute();
    }
    
    public static class CounterSource extends RichParallelSourceFunction<Long> implements CheckpointedFunction {
        private boolean running = true;
        
        private Long offset = 0L;
        
        // 声明一个算子状态变量
        // 列表状态变量中，只保存一个元素，保存刚消费完的偏移量
        // 算子状态不能使用ValueState
        private ListState<Long> state;
        @Override
        public void run(SourceContext<Long> ctx) throws Exception {
            final Object lock = ctx.getCheckpointLock();
            
            while (running) {
                // 加锁
                synchronized (lock) {
                    ctx.collect(offset);
                    offset += 1L;
                }
                Thread.sleep(1000L);
            }
        }
        
        @Override
        public void cancel() {
            running = false;
        }
        
        // 程序启动时触发调用（不管是第一次启动还是故障恢复）
        @Override
        public void initializeState(FunctionInitializationContext ctx) throws Exception {
            state = ctx.getOperatorStateStore().getListState(
              new ListStateDescriptor<Long>(
                "state",
                Types.LONG
              )
            );
            
            // 如果state中不为空，说明程序是故障恢复之后的启动，所以里面的元素就是刚消费完的偏移量
            // 如果程序是第一次启动，循环不执行
            // 如果是故障恢复启动，那么循环执行一次
            for (Long l : state.get()) {
                offset = l;
            }
        }
        
        // 当检查点分界线进入source算子时触发调用
        @Override
        public void snapshotState(FunctionSnapshotContext ctx) throws Exception {
            // 保存刚消费完的偏移量
            state.clear();
            state.add(offset);
        }
    }
}
