package _01_tools;

import java.sql.Timestamp;

public class POJO_UserViewCountPerWindow {
    String username;
    Long count;
    Long startTime;
    Long endTime;
    
    @Override
    public String toString() {
        return "UserViewCountPerWindow{" +
                 "username='" + username + '\'' +
                 ", count='" + count + '\'' +
                 
                 // NOTE 输出形式, 随意定义, 输出形式为
                 ", startTime=" + new Timestamp(startTime) +
                 ", endTime=" + new Timestamp(endTime) +
                 '}';
    }
    
    public POJO_UserViewCountPerWindow(String username, Long count, Long startTime, Long endTime) {
        this.username = username;
        this.count = count;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public POJO_UserViewCountPerWindow(String username) {
        this.username = username;
    }
}
