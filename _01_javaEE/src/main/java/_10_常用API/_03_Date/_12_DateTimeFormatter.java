package _10_常用API._03_Date;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class _12_DateTimeFormatter {
    public static void main(String[] args) {
        DateTimeFormatter isoDateTime = DateTimeFormatter.ISO_DATE_TIME;
    
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        
    
    
        System.out.println("isoDateTime.format(LocalDateTime.now()) = " + isoDateTime.format(LocalDateTime.now()));
        System.out.println("dateTimeFormatter.format(LocalDateTime.parse(\"1994-01-09\", dateTimeFormatter)) = " + dateTimeFormatter.format(LocalDateTime.parse("1994-01-09", dateTimeFormatter)));
    }
}
