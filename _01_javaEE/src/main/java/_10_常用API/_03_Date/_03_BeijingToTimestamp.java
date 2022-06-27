package _10_常用API._03_Date;

import java.sql.Timestamp;
import java.util.Date;

public class _03_BeijingToTimestamp {
    /*
    获取时间戳
        输出结果
            System.currentTimeMillis() = 1656321989874
            timestamp.getTime() = 1656321989874
            date.getTime() = 1656321989874
     */
    public static void main(String[] args) {
        Date date = new Date();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());
        System.out.println("timestamp.getTime() = " + timestamp.getTime());
        System.out.println("date.getTime() = " + date.getTime());
    }
    
}
