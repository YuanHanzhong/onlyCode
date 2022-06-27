package _10_常用API._03_Date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class _09_ZoneDateTime {
    public static void main(String[] args) {
        System.out.println("ZonedDateTime.now() = " + ZonedDateTime.now());
    
        ZonedDateTime parse = ZonedDateTime.parse("1994-01-09", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    
        System.out.println("parse = " + parse);
        
        ChronoUnit.DAYS.between(parse, parse);
    
    }
    
}
