package _07_面向对象;/*
 *需求:声明一个日期类MyDate，包含属性：年、月、日

在测试类的main方法中，创建3个日期对象，一个是你的出生日期，一个是来尚硅谷的日期，一个是毕业的日期，并打印显示

声明一个日期类MyDate，

-   包含属性：年、月、日
-   boolean isLeapYear()：判断是否是闰年
-   String monthName()：根据月份值，返回对应的英语单词
-   int totalDaysOfMonth()：返回这个月的总天数
-   int totalDaysOfYear()：返回这一年的总天数
-   int daysOfTheYear()：返回这一天是当年的第几数


 *要点:
 *      1
 *      2
 *      3
 */

public class Riqi {
    int year;
    int month;
    int day;
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public int getMonth() {
        return month;
    }
    
    public void setMonth(int month) {
        this.month = month;
    }
    
    public int getDay() {
        return day;
    }
    
    public void setDay(int day) {
        this.day = day;
    }
    
    
    boolean isLeapYear() {
        //if(year % 100)  if return 后面只有1个语句, true 或者false的时候, 直接return就好
        return year%4 == 0 && year %100 !=0 || year%400 ==0;
        // 闰年的频率还是很高的
    }
    
}
