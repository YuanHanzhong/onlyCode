package _03_流程控制.下;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import java.util.Scanner;

public class 第4题_接收2个正数 {
    
    // 输入两个正数, 判断后在输入, 不退出
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int input;
        // 键盘循环输入
        do {
            input = scanner.nextInt();
            
            // m, n 用来接收2个正数
            int m=0;
            int n=0;
            
            if (input < 0) {
                System.out.println("请重新输入一个正数: ");
            } else {
                m=input; // 局部变量的声明周期就在括号内
            }
    
            input = scanner.nextInt();
            if (input < 0) {
                System.out.println("请重新输入2个正数: ");
            } else {
                n=input;
            }
            System.out.println("m = " + m);
            System.out.println("n = " + n);
            
        } while (input != -1);
    }
    
    
    
}
