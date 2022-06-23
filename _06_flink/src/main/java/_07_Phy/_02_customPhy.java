package _07_Phy;

import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class _02_customPhy {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        DataStreamSource<Integer> integerDataStreamSource = executionEnvironment.fromElements(1, 2, 3, 4, 5, 6, 7, 8);
        
         //custom
        integerDataStreamSource
          .partitionCustom(
            new Partitioner<Integer>() {
                @Override
                public int partition(Integer key, int numPartitions) {
                    if (key == 1  || key ==2) {
                        return 1;
                    } else {
                        return 2;
                    }
                }
            }
            ,
            new KeySelector<Integer, Integer>() {
                @Override
                public Integer getKey(Integer value) throws Exception {
                    return value % 3;
                }
            })
          .print("cuttom: 1 4 7 2 8放到第二个slot, 其他随机")
          .setParallelism(4) // 这里要设置并行度, 否则报数组越界
    
        ;
        
        
        executionEnvironment.execute();
    }
    
}
