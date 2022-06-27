package _10_常用API._03_Date;

import java.text.SimpleDateFormat;

public class _04_SimpleDateFormate {
    public static void main(String[] args) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss:SSS'Z'");
    
        System.out.println("simpleDateFormat.format(System.currentTimeMillis()) = " + simpleDateFormat.format(System.currentTimeMillis()));
    }
}
