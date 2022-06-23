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
public class _10_Temperature_my {
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
        private ValueState<Double> latestTemperature;
    
        // timerTs.value() 作为另一个触发机关,
        //      报警后要清空,
        //      温度下降后也要清空
        private ValueState<Long> timerTs;
 
        
        
        @Override
        public void open(Configuration parameters) throws Exception {
            latestTemperature = getRuntimeContext().getState(
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
    //
    //        // 处理温度
    //
    //        Double latestTemp = latestTemperature.value(); // got 这里是执行的关键
    //        latestTemperature.update(in.temperature);
    //// got 判断非空很重要
    //        // 需要保证上一次温度是存在的
    //        if (latestTemp != null) {
    //            // 第一种情况：温度上升 && 报警定时器不存在
    //            if (in.temperature > latestTemp && timerTs.value() == null) {
    //                Long oneSecondLater = ctx.timerService().currentProcessingTime() + 1000L;
    //                // 注册1s之后的定时器
    //                ctx.timerService().registerProcessingTimeTimer(
    //                  oneSecondLater
    //                );
    //                // 将定时器的时间戳保存下来
    //                timerTs.update(oneSecondLater);
    //            }
    //            // 第二种情况：温度下降 && 存在报警定时器
    //            else if (in.temperature < latestTemp && timerTs.value() != null) {
    //                // 手动从定时器队列中删除定时器
    //                ctx.timerService().deleteProcessingTimeTimer(timerTs.value());
    //                // 将保存的定时器的时间戳删除
    //                timerTs.clear();
    //                System.out.println("xxxxxxxxxxxxxxxxxxx" + timerTs.value());
    //            }
    //        }
    
    
            long currentTimeMill = ctx.timerService().currentProcessingTime();
            // star
            Double latestTemp = latestTemperature.value(); // got 这里是执行的关键, 先存旧的
            latestTemperature.update(in.temperature); // got 必要
    
            if (latestTemp != null) { //got 必须保证 **上一次** 的非空, 否则定时器
        
                // star 输出的信息, 范围范围越小越好
        
                // star 哪里没有执行, 直接拆分输出判断条件, 而不是用脑子算
                //System.out.println("(previousTemperature.value()<latestTemperature.value()) = " + (previousTemperature.value() < latestTemperature.value()));
                System.out.println("(timerTs == null) = " + (timerTs == null));
        
        
                if (timerTs.value() == null && in.temperature > latestTemp) { // got 状态变量不能直接使用, 要用value
                    System.out.println("正在设置定时器, 时间是: " + currentTimeMill + 1000L);
                    ctx.timerService().registerProcessingTimeTimer(currentTimeMill + 1000L);
                    timerTs.update(currentTimeMill + 1000L);
            
                } else if (timerTs.value() != null && in.temperature < latestTemp) {
                    System.out.println("正在删除定时器, 时间是: " + currentTimeMill + 1000L);
            
                    ctx.timerService().deleteProcessingTimeTimer(timerTs.value());
                    timerTs.clear();
                }
        
            }
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
                          "当前温度: " + latestTemperature.value() + "\n" +
                          "上一秒温度: \n" +
                          "到达的时间是: " + new Timestamp(timerTs.value()));
            timerTs.clear();
        }
    }
}
