package _19_FlinkSQL;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

public class _07_TableAPI {
    public static void main(String[] args) {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
    
        
        EnvironmentSettings settings = EnvironmentSettings
                                      .newInstance()
                                      .inStreamingMode()
                                      .build();
    
        TableEnvironment tableEnvironment = TableEnvironment.create(settings);
        
        //tableEnvironment.executeSql(
        //  "CREATE TEMPORARY TABLE table1"
        //)
    
    }
    
}
