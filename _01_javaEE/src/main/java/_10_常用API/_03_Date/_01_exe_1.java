package _10_常用API._03_Date;

import org.junit.Test;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;


public class _01_exe_1 {

    
    @Test
    public void test2() {
        /*
         * TimeZone
         */
    
        System.out.println("TimeZone.getDefault() = " + TimeZone.getDefault());
        System.out.println("TimeZone.getTimeZone(\"GMT\") = " + TimeZone.getTimeZone("GMT"));
        System.out.println("TimeZone.getTimeZone(\"GMT\") = " + TimeZone.getTimeZone("GMT"));
        System.out.println("TimeZone.getAvailableIDs() = " + Arrays.toString(TimeZone.getAvailableIDs()));
    }
    
    @Test
    public void test3() {
        /*
         * Locale
         */
        Locale china = Locale.CHINA;
        System.out.println("china = " + china);
    }
    
    @Test
    public void test4() {
        /*
         * JDK8之后
         */
        System.out.println("LocalDate.now() = " + LocalDate.now());
    
        System.out.println("ZonedDateTime.now() = " + ZonedDateTime.now(ZoneId.of("UTC")));
        System.out.println("ZonedDateTime.now() = " + ZonedDateTime.now(ZoneId.of("UTC+8")));
    
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC+8"));
        //
    
    
        //System.out.println("ZonedDateTime.now() = " + ZonedDateTime.now(ZoneId.of("utc"))); // ZoneId 都要大写
        //System.out.println("ZonedDateTime.now() = " + ZonedDateTime.now(ZoneId.of("CTT")));
        //System.out.println("ZonedDateTime.now() = " + ZonedDateTime.now(ZoneId.of("UTC+8")));
    }
    
    @Test
    public void test01(){
        Date d = new Date();
        System.out.println(d);//现在的系统日期时间值
        //例如：Sat Feb 12 10:35:25 CST 2022
    }
    
    @Test
    public void test02(){
        Date d = new Date();
        System.out.println(d.getTime());//获取某个日期时间对应的毫秒值，距离1970-1-1 0:0:0 0毫秒的时间差
        //如果Date对象是系统时间的话，等价于System.currentTimeMills()
    }
    
    @Test
    public void test03(){
        //根据毫秒值，创建一个Date对象
        Date d = new Date(100000000000L);
        System.out.println(d);//Sat Mar 03 17:46:40 CST 1973
    
        System.out.println("Integer.MAX_VALUE = " + Integer.MAX_VALUE);
        System.out.println("Long.MAX_VALUE = " + Long.MAX_VALUE);
    
        System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());
    
        Date d2 = new Date(Integer.MAX_VALUE);
        System.out.println(d2);
        
        Date d3 = new Date(Long.MAX_VALUE);
        System.out.println(d3);
    }
    
    @Test
    public void test04(){
        Date d = new Date();
        System.out.println(d);//Sat Feb 12 10:40:19 CST 2022
        
        //日期格式化
        SimpleDateFormat sf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss SSS E");
        String str = sf.format(d);
        System.out.println("str = " + str);//2022年02月12日 10:43:04 578 星期六
    }
    
    @Test
    public void test05(){
        String[] availableIDs = TimeZone.getAvailableIDs();//Available可用的 ID时区编号
        for (String availableID : availableIDs) {
            System.out.println(availableID);
        }
    }
    
    @Test
    public void test06(){
        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        System.out.println(timeZone);
    }
    
    @Test
    public void test07(){
//        Calendar c = new GregorianCalendar();
        Calendar c = Calendar.getInstance();//按照当前运行环境的时区
        System.out.println(c);
        
        //get方法配合Calendar类的常量，来指定是哪个日期或时间值
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;//默认从0开始。
        int day = c.get(Calendar.DATE);
        int hour1 = c.get(Calendar.HOUR_OF_DAY);//24小时制
        int hour2 = c.get(Calendar.HOUR);//12小时制
        int minute = c.get(Calendar.MINUTE);
        
        System.out.println(year + "-" + month + "-" + day + " " + hour1 + ":" + minute);
    }
    
    @Test
    public void test08(){
//        Calendar c = new GregorianCalendar();
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"), Locale.US);//按照当前运行环境的时区
        System.out.println(c);
        
        //get方法配合Calendar类的常量，来指定是哪个日期或时间值
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;//默认从0开始。
        int day = c.get(Calendar.DATE);
        int hour1 = c.get(Calendar.HOUR_OF_DAY);//24小时制
        int hour2 = c.get(Calendar.HOUR);//12小时制
        int minute = c.get(Calendar.MINUTE);
        
        System.out.println(year + "-" + month + "-" + day + " " + hour1 + ":" + minute);
    }
}
