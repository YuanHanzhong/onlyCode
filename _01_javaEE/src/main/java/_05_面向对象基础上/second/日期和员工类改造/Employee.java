package _05_面向对象基础上.second.日期和员工类改造;/*
 *需求:
 （2）声明另一个Employee类型，

-   有属性：姓名（String类型），生日（MyDate类型）
-   增加一个void setBirthday(int year, int month, int day)方法，用于给员工生日赋值
-   增加一个String getEmpInfo()方法，用于返回员工对象信息，例如：姓名：xx，生日：xx年xx月xx日

 *要点:
 *      1
 *      2
 *      3
 */

public class Employee {
    
    private String name;
    private MyDate birthday=new MyDate(); // got 声明自定义的类型的变量时,  一定要new 一下
    
    public void setName(String name) {
        this.name = name;
    }
    
    void setBirthday(int year, int month, int day) {
        birthday.setYear(year);
        birthday.setDay(day);
        birthday.setMonth(month);
    }
    
    String getEmployInfo() {
        return "姓名: "+name+ "生日: "+birthday.getInfo();
    }
}
