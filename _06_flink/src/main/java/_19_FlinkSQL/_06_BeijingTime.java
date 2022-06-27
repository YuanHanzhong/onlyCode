package _19_FlinkSQL;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

import java.time.ZoneId;

public class _06_BeijingTime {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
    
        EnvironmentSettings settings = EnvironmentSettings.newInstance().build();
    
    
        TableEnvironment tableEnvironment = TableEnvironment.create(settings);
    
        tableEnvironment.getConfig().setLocalTimeZone(ZoneId.of("GMT+8"));
    
        System.out.println("tableEnvironment.getConfig() = " + tableEnvironment.getConfig());
    
    
        executionEnvironment.execute();
    }
    
}
