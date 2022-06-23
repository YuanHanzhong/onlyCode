package _14_lateness;

import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class _03_SideoutPut_teacher_my {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        SingleOutputStreamOperator<String> result =
          env
            .addSource(new SourceFunction<String>() {
                @Override
                public void run(SourceContext<String> ctx) throws Exception {
                    ctx.collectWithTimestamp("a", 1000L);
                    ctx.emitWatermark(new Watermark(999L));
                    ctx.collectWithTimestamp("a", 2000L);
                    ctx.emitWatermark(new Watermark(1999L));
                    ctx.collectWithTimestamp("a", 1500L);
                }
                
                @Override
                public void cancel() {
                
                }
            })
            .process(new ProcessFunction<String, String>() {
                @Override
                public void processElement(String in, Context ctx, Collector<String> out) throws Exception {
                    if (ctx.timestamp() > ctx.timerService().currentWatermark()) {
                        out.collect("数据" + in + "的事件时间是：" + ctx.timestamp() + "," +
                                      "当前水位线是：" + ctx.timerService().currentWatermark());
                    } else {
                        ctx.output(
                          // 测输出流
                          new OutputTag<String>("late-event") {
                          },
                          "数据" + in + "的事件时间是：" + ctx.timestamp() + ",当前水位线是：" +
                            "" + ctx.timerService().currentWatermark()
                        );
                    }
                }
            });
        
        result.print("主流");
    
        //result.getSideOutput(new OutputTag<String>("late-event") {        }).print("侧输出流");
        
        result
          .getSideOutput(new OutputTag<String>("late_event"){}) // new 函数的话, 要用{} ask
          .print();
        
        env.execute();
    }
}
