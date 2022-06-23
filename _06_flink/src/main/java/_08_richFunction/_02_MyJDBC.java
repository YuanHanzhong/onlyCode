package _08_richFunction;

import _06_exam.exeClickEvent;
import _06_exam.exeClickSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

// create database userbehavior;
// use userbehavior;
// create table clicks (username varchar(100), url varchar(100));
public class _02_MyJDBC {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new exeClickSource())
          .addSink(new MyJDBC());
        
        env.execute();
    }
    
    public static class MyJDBC extends RichSinkFunction<exeClickEvent> {
        private Connection connection;
        private PreparedStatement insertStmt;
        private PreparedStatement updateStmt;
        @Override
        public void open(Configuration parameters) throws Exception {
            connection = DriverManager.getConnection(
              "jdbc:mysql://hadoop102:3306/userbehavior?useSSL=false", // JDBC 语法
              "root",
              "root"
            );
            
            insertStmt = connection.prepareStatement(
              "insert into clicks (username, url) values (?,?)"
            );
            updateStmt = connection.prepareStatement(
              "update clicks set url = ? where username = ?"
            );
        }
        
        @Override
        public void invoke(exeClickEvent in, Context context) throws Exception {
            // 每来一条数据调用一次
            
            // 幂等性的写入mysql，每个username只有一条数据
            
            // 先进行更新
            updateStmt.setString(1, in.url);
            updateStmt.setString(2, in.username);
            updateStmt.execute();
            System.out.println("data setted");
            
            // 如果更新的行数为0,说明in.username对应的数据在表中不存在
            // 所以插入数据
            if (updateStmt.getUpdateCount() == 0) {
                insertStmt.setString(1, in.username);
                insertStmt.setString(2, in.url);
                insertStmt.execute();
                System.out.println("data inserted");
            }
        }
        
        @Override
        public void close() throws Exception {
            insertStmt.close();
            updateStmt.close();
            connection.close();
        }
    }
}
