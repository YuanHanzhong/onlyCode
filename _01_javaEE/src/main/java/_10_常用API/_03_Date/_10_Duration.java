package _10_常用API._03_Date;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class _10_Duration {
    public static void main(String[] args) {
        LocalTime end = LocalTime.of(20, 00, 00);
    
        Duration between = Duration.between(LocalTime.now(), end);
    
        System.out.println("between = " + between.toMinutes());
    
    }
    
}
