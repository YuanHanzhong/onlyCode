package _08_richFunction;

import com.atguigu.utils.SensorReading;
import com.atguigu.utils.SensorSource;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;

// 连续1秒钟温度上升的检测
public class _10_Temperature_my_3 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new SensorSource())
          .keyBy(r -> r.sensorId)
          .process(new TempAlert())
          .print();
        
        env.execute();
    }
    
    
    public static class TempAlert extends KeyedProcessFunction<String, SensorReading, String> {
        
        // 先想key需要维护哪些state, 这里写了, open中就一定要指定下, 初始化下, 否则用不了
        private ValueState<Double> lastTemp;
        private ValueState<Long> timerTs;
        
        
        @Override
        public void open(Configuration parameters) throws Exception {
            lastTemp = getRuntimeContext().getState(
              new ValueStateDescriptor<Double>(
                "last-temp",
                Types.DOUBLE
              )
            );
            timerTs = getRuntimeContext().getState(
              new ValueStateDescriptor<Long>(
                "timer-ts",
                Types.LONG
              )
            );
        }
        
        @Override
        public void processElement(SensorReading in, Context ctx, Collector<String> out) throws Exception {
            
            // 处理温度
            
            Double prevTemp = lastTemp.value();
            lastTemp.update(in.temperature);
            
            // ts timerTs 作为另一个报警时触发机关, 用来判断有没有注册过定时器
            //      报警后要清空,
            //      温度下降后也要清空
            Long ts = timerTs.value();
            
            // 小到一个函数时, 先写伪代码比较好  star
            // 大的函数, 也是先写出框架来比较好 star
            // 容易忘得, 想到就写上 star
            
            // 保证温度维度, 有可比性, 保证大前提即上一次温度为非空
            //      若没有注册过并且温度升高
            //          就注册1次, 主备报警
            //      若注册过但温度下降了
            //          就删除注册过的, 不让其报警
            //              取消的话
            //                  手动删除报警器
            //                  把机关ts复原
    
    
    
    
            if (prevTemp != null) {
                if (ts == null) {
                    if (in.temperature > prevTemp) {

                        Long oneSecondLater = ctx.timerService().currentProcessingTime() + 1000L;
                        ctx.timerService().registerProcessingTimeTimer(oneSecondLater);

                        // 保存最新的时间戳, 说明来过, 定时一次就行
                        timerTs.update(oneSecondLater);
                    }
                }
             
                else if (in.temperature < prevTemp) {
                    ctx.timerService().deleteProcessingTimeTimer(ts);
                    // 将保存的定时器的时间戳删除
                    timerTs.clear();
                }
            }

      
             //got 判断非空很重要
            
            
        }
        
        // new Timestamp got 北京时间
        
        // 报警定时器
        // 由于触发onTimer执行时，flink会自动将定时器从定时器队列中删除
        // 所以这里我们需要将保存定时器时间戳的timerTs删除
        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            // 触发
            //      自动出列定时器
            //      手动标记null
            out.collect("传感器" + ctx.getCurrentKey() + "连续1s温度上升了！" + "\n" +
                          "当前温度: " + lastTemp.value() + "\n" +
                          "上一秒温度: \n" +
                          "到达的时间是: " + new Timestamp(timerTs.value()));
            timerTs.clear();
        }
    }
}
