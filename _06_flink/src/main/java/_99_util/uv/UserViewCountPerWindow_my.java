package _99_util.uv;

import java.sql.Timestamp;

/*
需求: 定义每个窗口的POJO
    窗口
        startTime
        endTime
    必要的2个标识
        username
        url
    
GOT
    定义POJO时, 参数顺序比较重要, 因为构造函数是根据参数顺序来的
定义输出数据类型
 */
public class UserViewCountPerWindow_my {
    String username;
    Long count;
    Long stratTime;
    Long endTime;
    
    @Override
    public String toString() {
        return "UserViewCountPerWindow_my{" +
                 "username='" + username + '\'' +
                 ", count=" + count +
                 ", stratTime=" + stratTime +
                 ", endTime=" + endTime +
                 '}';
    }
    
    public UserViewCountPerWindow_my(String username, Long count, Long stratTime, Long endTime) {
        this.username = username;
        this.count = count;
        this.stratTime = stratTime;
        this.endTime = endTime;
    }
    
    public UserViewCountPerWindow_my() {
    }
}
