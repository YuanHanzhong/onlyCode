package _10_常用API._03_Date;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class _07_LocalDate {
    public static void main(String[] args) {
    
        LocalDateTime now = LocalDateTime.now();
    
        System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
        System.out.println("LocalDateTime.now().getDayOfMonth() = " + LocalDateTime.now().getDayOfMonth());
    
    
        System.out.println("LocalDate.now().isLeapYear() = " + LocalDate.now().isLeapYear());
    
    }
    
}
