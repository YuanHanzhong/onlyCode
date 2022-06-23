package _05_DIY;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class _05_MapTupple {
    /**
     * 把整数转为Tupple2
     */
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        DataStreamSource<Integer> integerDataStreamSource = executionEnvironment
                                                              .fromElements(1, 2, 3, 4);
        
        integerDataStreamSource
          .map(r -> Tuple2.of(r, r))
          .returns(Types.TUPLE(
            Types.INT,
            Types.INT
          
          ))
          .print();
        
        executionEnvironment.execute();
    }
}
