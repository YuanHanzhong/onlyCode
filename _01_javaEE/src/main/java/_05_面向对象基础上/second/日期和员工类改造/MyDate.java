package _05_面向对象基础上.second.日期和员工类改造;/*
 *需求:）声明一个MyDate类型

-   有属性：年，月，日
-   增加一个String getDateInfo()方法，用于返回日期对象信息，例如：xx年xx月xx日

 *要点:
 *      1
 *      2
 *      3
 */

public class MyDate {
    private int year;
    private int month;
    private int day;
    
    String getInfo() {
        return ""+year+"年 "+month+"月 "+day +"日"; // got 少些了 + 导致编译报错
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public void setMonth(int month) {
        this.month = month;
    }
    
    public void setDay(int day) {
        this.day = day;
    }
}
