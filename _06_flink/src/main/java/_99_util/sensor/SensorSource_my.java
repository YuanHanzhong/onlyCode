package _99_util.sensor;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.ArrayList;
/*
需求: 采样间隔为1s, 连续3次采样, 温度都升高, 则报警. 温度下降一次就抵消一次升高. 报警之后, 10s之内不再报警.
    分解需求:
        1. 连续升高2次,
        2. 降低1次
        3. 连续升高2次, 报警
        4. 接下来10秒, 连续升高3次, 不报警
        5. 从报警后, 第11秒起, 再连续升高3次, 报警
 */


public class SensorSource_my implements SourceFunction<SensorReading_my> {
    Boolean isRuning = true;
    ArrayList<Double> myDataList = new ArrayList<>();
    
    @Override
    public void run(SourceContext<SensorReading_my> ctx) throws Exception {
        // 为了简化问题, 不涉及水位线, 这里用while循环
            
            
            //1. 连续升高2次,
            myDataList.add(1D);
            myDataList.add(2D);
            myDataList.add(3D);
            //2. 降低1次
            myDataList.add(1D);
            
            //3. 连续升高2次, 报警
            myDataList.add(2D);
            myDataList.add(3D);
            
            
            //4. 接下来10秒, 连续升高3次, 不报警. 降低时, count也不减少.
            myDataList.add(3D);
            myDataList.add(3D);
            myDataList.add(3D);
            myDataList.add(5D);
            myDataList.add(6D);
            myDataList.add(5D);
            // 4个降低
            myDataList.add(4D);
            myDataList.add(3D);
            myDataList.add(1D);
            myDataList.add(1D);
            
            
            //5. 从报警后的第11秒起, 再连续升高3次, 报警
            myDataList.add(2D);
            myDataList.add(3D);
            myDataList.add(4D);
            myDataList.add(5D);
            myDataList.add(5D);
            myDataList.add(5D);
            
            // 什么时候不需要设置水位线, 只要用的窗口或者定时器都要水位线吗?
            //  kafka中最后一部分数据处理不了, 那些数据正好是自动添加的最后一个水位线之后的数据吧? 这些数据会在run()结束后, 立刻被丢弃吗?
            
            //
            for (Double aDouble : myDataList) {
                ctx.collect(new SensorReading_my(
                  // 为简化问题, 这里就默认只有一个传感器
                  "sensor_",
                  aDouble
                ));
                Thread.sleep(1000L);
            }
            
        //}
    }
    
    
    @Override
    public void cancel() {
        isRuning = false;
    }
}
