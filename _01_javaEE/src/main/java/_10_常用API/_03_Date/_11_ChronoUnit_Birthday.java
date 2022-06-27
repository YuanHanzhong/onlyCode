package _10_常用API._03_Date;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class _11_ChronoUnit_Birthday {
    /*
    计算活了多少天
     */
    
    public static void main(String[] args) {
        LocalDate birthday = LocalDate.of(1994, 1, 9);
    
        System.out.println("aliveDays= " + ChronoUnit.DAYS.between(birthday, LocalDate.now()));
        System.out.println("aliveMonths= " + ChronoUnit.MONTHS.between(birthday, LocalDate.now()));
        System.out.println("aliveYears= " + ChronoUnit.YEARS.between(birthday, LocalDate.now()));
        
        
    
    }
    
}
