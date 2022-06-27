package _17_;

import com.atguigu.utils.ClickSource;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class _03_CheckPoint {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        // 每隔10s保存一次检查点
        env.enableCheckpointing(10 * 1000L);
        // 设置检查点文件夹的绝对路径
        // file:// + 文件夹的绝对路径, windows的路径为反斜杠
    
        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage("file:\\D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\checkpoints\\checkpoint"); // 这里默认为文件夹, 不是具体的文件
        
        env.addSource(new ClickSource()).print();
        
        env.execute();
    }
}
