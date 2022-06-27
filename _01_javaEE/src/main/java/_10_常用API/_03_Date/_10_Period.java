package _10_常用API._03_Date;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;

public class _10_Period {
    public static void main(String[] args) {
        Period period = Period.between(LocalDate.of(1994, 1, 9), LocalDate.now());
    
        System.out.println("period = " + period);
    
        System.out.println("aliveDays = " + period.getYears());
        System.out.println("aliveMonths = " + period.getMonths());
        System.out.println("aliveYears = " + period.toTotalMonths());
        

        

    }
    
}
