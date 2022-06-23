package _05_面向对象基础上.第一次练习;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import org.junit.Test;

public class Employee {
    private String name;
    private MyDate birthday = new MyDate(); // got 自己定义的类, 要new一下才能用
    
    
    void setBirthday(int year, int month, int day) {
        birthday.setYear(year);
        birthday.setMonth(month);
        birthday.setDay(day);
        
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String toString() {
        return "Employee{" +
                       "name='" + name + '\'' +
                       ", birthday=" + birthday.getDateInfo() +
                       '}';
    }
    
    @Test
    public void test() {
        /**
         * 题目：测试本类
         * 要点：
         */
        Employee employee = new Employee();
        employee.setBirthday(2012, 8, 30);
        employee.setName("Jack");
        System.out.println(employee.toString());
    }
    
    public static void main(String[] args) {
        Employee employee = new Employee();
        employee.setBirthday(2012, 8, 30);
        employee.setName("Jack");
        employee.toString();
    }
}
