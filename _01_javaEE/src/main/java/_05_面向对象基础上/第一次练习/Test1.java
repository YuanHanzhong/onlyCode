package _05_面向对象基础上.第一次练习;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import org.junit.Test;

import java.util.Scanner;

public class Test1 {
    
    @Test // got junit单元测试的@test不能和本类的类名一样
    public void test2() {
        /**
         * 题目：在测试类中的main中，创建两个员工对象，并为他们的姓名和生日赋值，并显示
         * 要点：
         */
        Scanner scanner = new Scanner(System.in);
        Employee employee = new Employee();
        Employee employee1 = new Employee();
    
        System.out.println("请输入年/月/日/姓名, 回车隔开");
        employee.setBirthday(scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
        scanner.nextLine();
        employee.setName(scanner.nextLine());
    
        System.out.println("请输入年/月/日/姓名, 回车隔开");
        employee1.setBirthday(scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
        scanner.nextLine();
        employee1.setName(scanner.nextLine());
    
        System.out.println(employee.toString());
        System.out.println(employee1.toString());
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Employee employee = new Employee();
        Employee employee1 = new Employee();
    
        System.out.println("请输入年/月/日/姓名, 回车隔开");
        employee.setBirthday(scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
        //employee.setName(scanner.nextLine());
        //
        //System.out.println("请输入年/月/日/姓名, 回车隔开");
        //employee1.setBirthday(scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
        //employee1.setName(scanner.nextLine());
    }
   
}


