package _10_常用API._03_Date;

import java.util.Calendar;
import java.util.TimeZone;

public class _06_Calendar {
    public static void main(String[] args) {
        System.out.println("Calendar.getInstance().get(Calendar.YEAR) = " + Calendar.getInstance().get(Calendar.YEAR));
        System.out.println("Calendar.getInstance().get(Calendar.DAY_OF_MONTH) = " + Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_MONTH));
    }
}
