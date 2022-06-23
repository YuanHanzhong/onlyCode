package _08_richFunction;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public class _04_Alert {
    public static void main(String[] args) throws Exception {
        
        // processfunction, 加强版的Richflatmap()
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        DataStreamSource<String> sourceNet = executionEnvironment.socketTextStream("hadoop102", 9999);
        
        
        executionEnvironment
          .fromElements("1", "2", "3")
          .process(new ProcessFunction<String, String>() {
              @Override
              public void processElement(String value, Context ctx, Collector<String> out) throws Exception {
                  out.collect("--->" + value);
              }
          })
          .print("my");
        
        
        executionEnvironment
          .fromElements("white", "black", "gray")
          .process(new ProcessFunction<String, String>() {
              @Override
              public void processElement(String in, Context ctx, Collector<String> out) throws Exception {
                  out.collect(in);
                  
              }
          })
          .print("teacher");
        
        
        executionEnvironment.execute();
    }
    
}
