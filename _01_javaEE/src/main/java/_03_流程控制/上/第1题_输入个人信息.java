package _03_流程控制.上;/*
 *需求:从键盘输入个人的信息，用合适的变量接收并输出。例如：姓名、年龄、性别、体重、婚否等
 *要点:
 *      1
 *      2
 *      3
 */

import java.util.Scanner;

public class 第1题_输入个人信息 {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int age;
        String name;
    
        System.out.println("name: ");
        name = scanner.next();
        System.out.println("age: ");
        age = scanner.nextInt();
    
        System.out.println("name = " + name);
        System.out.println("age = " + age);
        
    }
    
}
