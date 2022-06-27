package _10_常用API._03_Date;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class _02_TimestampToBeijing {
    /*
    获取UTC

     */
    
    public static void main(String[] args) {
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
    
        System.out.println("new Date() = " + new Date());
        System.out.println("new Date(1656319827802L) = " + new Date(1656319827802L));
    
        System.out.println("date.toString() = " + date.toString());
        System.out.println("date.toInstant() = " + date.toInstant());
        
        System.out.println("timestamp.toString() = " + timestamp.toString());
        System.out.println("timestamp.toInstant() = " + timestamp.toInstant());
        System.out.println("timestamp.toLocalDateTime() = " + timestamp.toLocalDateTime());
    
        System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
        System.out.println("LocalDateTime.of(2022, 6, 7,11,30) = " + LocalDateTime.of(2022, 6, 7, 11, 30));
    }

}
