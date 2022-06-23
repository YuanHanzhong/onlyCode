package _07_Phy;

import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class _01_exe {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        DataStreamSource<Integer> integerDataStreamSource = executionEnvironment.fromElements(1, 2, 3, 4, 5, 6, 7, 8);
        // shuffled
        integerDataStreamSource
          .shuffle()
          .print("shuffle: ");

        // rebalance
        integerDataStreamSource
          .rebalance()
          .print("rebalance");

        
        
        executionEnvironment.execute();
    }
    
}
