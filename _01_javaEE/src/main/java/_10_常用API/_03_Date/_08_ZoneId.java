package _10_常用API._03_Date;

import java.time.ZoneId;

public class _08_ZoneId {
    public static void main(String[] args) {
        System.out.println("ZoneId.getAvailableZoneIds().size() = " + ZoneId.getAvailableZoneIds().size());
    
        System.out.println("ZoneId.systemDefault() = " + ZoneId.systemDefault());
        
    }
    
}
