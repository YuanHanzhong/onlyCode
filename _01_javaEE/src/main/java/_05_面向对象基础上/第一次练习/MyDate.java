package _05_面向对象基础上.第一次练习;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import org.junit.Test;

import java.util.Scanner;

public class MyDate {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MyDate myDate = new MyDate();
        myDate.setDay(scanner.nextInt());
        myDate.setMonth(scanner.nextInt());
        myDate.setYear(scanner.nextInt());
    
        System.out.println(myDate.getDateInfo());
    }
    
    private int year;
    private int month;
    private int day;
    public String getDateInfo() {
        return year + "年"+month+"月"+day+"日";
    }
    
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
    
    @Test
    public void test() {
        /**
         * 题目：
         * 要点：
         */
        Scanner scanner = new Scanner(System.in);
        MyDate myDate = new MyDate();
        myDate.setDay(scanner.nextInt());
        myDate.setMonth(scanner.nextInt());
        myDate.setYear(scanner.nextInt());
    
        System.out.println(myDate.getDateInfo());
    
    }
}
