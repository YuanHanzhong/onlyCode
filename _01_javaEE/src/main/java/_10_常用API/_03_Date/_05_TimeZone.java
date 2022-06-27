package _10_常用API._03_Date;

import java.util.TimeZone;

public class _05_TimeZone {
    public static void main(String[] args) {
        // getTimeZone
        System.out.println("TimeZone.getTimeZone(\"GMT\") = " + TimeZone.getTimeZone("GMT"));
    
        // getAvailableIDs
        String[] availableIDs = TimeZone.getAvailableIDs();
        for (String availableID : availableIDs) {
            System.out.println("availableID = " + availableID);
        }
    
        
        
    }
}
