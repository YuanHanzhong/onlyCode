package _08_richFunction;

import _99_util.sensor.SensorReading_my;
import _99_util.sensor.SensorSource_my;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;

/*
收获:
    STAR 做输出标签, 看程序执行到了哪里, System.out.println("-----------2");
    STAR 有个里程碑就应该保存下, 有时候把正确的都改没了
    STAR 借助了State为null来判断是否为第一次
    STAR 步骤:
        1. 造测试数据, 步步为营
        2. 大致分析下, 先写段代码输出下, 帮着自己分析



需求: 采样间隔为1s, 连续3次采样, 温度都升高, 则报警. 温度下降一次就抵消一次升高. 报警之后, 10s之内不再报警.
    分解需求:
        1. 实现连续3次升高报警, 下降的次数抵消掉上升的次数之后取消报警
        2. 实现10s之内只报一次

        
备注:
    3小时2分钟
    

 */

public class _10_Temperature_my_BOSS {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new SensorSource_my())
          .keyBy(r -> r.sensorId)
          .process(new TempAlert())
          .print();
        env.execute();
    }
    public static class TempAlert extends KeyedProcessFunction<String, SensorReading_my, String> {
       
        // 要单独拿出来
        // 保存最新温度, 好做对比
        private ValueState<Double> latestTemperature;
        
        // 为了搞定报警后, 10s内不再重复报警. 它只需要保证hibernate期间, count不变
        private ValueState<Long> hibenateTenSeconds;
       
        // 为了搞定连续3次
        private ValueState<Integer> counter;
        @Override
        public void open(Configuration parameters) throws Exception {
            latestTemperature = getRuntimeContext().getState(
              new ValueStateDescriptor<Double>(
                "lastest-temp",
                Types.DOUBLE
              )
            );
            hibenateTenSeconds = getRuntimeContext().getState(
              new ValueStateDescriptor<Long>("ten-seconds", Types.LONG)
            );
            counter = getRuntimeContext().getState(
              new ValueStateDescriptor<Integer>("counter", Types.INT)
            );
        }
        @Override
        public void processElement(SensorReading_my in, Context ctx, Collector<String> out) throws Exception {
            System.out.println("\n\n数据来了, 当前温度: " + in.temperature + "======================================");
            long currentTimeMill = ctx.timerService().currentProcessingTime();
            
            // 无条件初始化 hibernateTenSeconds 为currentTimeMill
            if (hibenateTenSeconds.value() == null) {
                hibenateTenSeconds.update(currentTimeMill);
            }
            
            // 为了保证在休眠期count数值不变
            if (hibenateTenSeconds.value() <= currentTimeMill) {
                if (counter.value() == null) {
                    counter.update(0);
                } else {
                    if (latestTemperature.value() < in.temperature) {
                        counter.update(counter.value() + 1);
                    } else if (latestTemperature.value() > in.temperature) {
                        counter.update(counter.value() - 1);
                    }
                }
                System.out.println("counter = " + counter.value());
                latestTemperature.update(in.temperature);
    
            }
            
            // 连续升高3次才register, register了就不取消
            if (counter.value() >= 3) {
                ctx.timerService().registerProcessingTimeTimer(currentTimeMill);
            }
        }
        
        
        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            out.collect(
              "报警了" + "\n" +
                "当前温度: " + latestTemperature.value() + "  \n" +
                "报警时间时间是: " + new Timestamp(timestamp) // GOT 定时器直接用系统的参数就好, 做到很好的解耦
            );
    
            // 报警后10s内不再报警
            hibenateTenSeconds.update(timestamp + 10 * 1000L);
            
            // 报警后清空计数器
            counter.update(0); // 重置下就行, 不需要clear 为 null
        }
    }
}
