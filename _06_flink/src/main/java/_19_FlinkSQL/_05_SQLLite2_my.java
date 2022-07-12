package _19_FlinkSQL;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class _05_SQLLite2_my {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment streamTableEnvironment = StreamTableEnvironment
                                                          .create(
                                                            env,
                                                            EnvironmentSettings.newInstance().inStreamingMode().build()
                                                          );
        
        streamTableEnvironment
          // 2022/7/11 18:13 NOTE TODO 一个executesql只能执行1条语句
          .executeSql(
            "create table clicks (" +
              "`user` STRING, " +
              "`url` STRING, " +
              "ts TIMESTAMP(3), " +
              //"`ts` BIGINT, "+
              //"time_ltz AS TO_TIMESTAMP_LTZ(ts, 3), "+
              "WATERMARK FOR ts AS ts - INTERVAL '3' SECONDS) " +
              "WITH (" +
              "'connector' = 'filesystem'," +
              "'path' = 'D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\file1.csv'," +
              "'format' = 'csv')"
              //+
              //"create table clicks2 (" +
              //"`user` STRING, " +
              //"`url` STRING, " +
              //"ts TIMESTAMP(3), " +
              ////"`ts` BIGINT, "+
              ////"time_ltz AS TO_TIMESTAMP_LTZ(ts, 3), "+
              //"WATERMARK FOR ts AS ts - INTERVAL '3' SECONDS) " +
              //"WITH (" +
              //"'connector' = 'filesystem'," +
              //"'path' = 'D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\file1.csv'," +
              //"'format' = 'csv')"
          );

        streamTableEnvironment
          .executeSql(
            "create table ResultTable (" +
              "`user` STRING, " +
              "windowEndTime TIMESTAMP(3), " +
              "cnt BIGINT)" +
              " WITH (" +
              "'connector' = 'print')"
          );
        
        streamTableEnvironment
          .executeSql(
            "insert into ResultTable " +
              "select user, " +
              "TUMBLE_END(ts, INTERVAL '1' HOURS) as windowEndTime, " +
              "count(user) as cnt " +
              "from clicks group by user, TUMBLE(ts, INTERVAL '1' HOURS)"
          );
    }
}
