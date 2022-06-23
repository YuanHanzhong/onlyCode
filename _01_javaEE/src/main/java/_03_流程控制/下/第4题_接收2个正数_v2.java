package _03_流程控制.下;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import java.util.Scanner;

public class 第4题_接收2个正数_v2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
    
    
        while (true) {
            int m=0;
            int n=0;
    
            // 接收正数, 否则一直循环 star
            while (true) {
                System.out.println("请输入1个正数");
        
                 n = scanner.nextInt();
                if (n < 0) {
                    System.out.println();
            
                } else {
                    break;
                }
            }
    // star 先循环, 循环里有判断, 符合要求break
            while (true) {
                System.out.println("请输入1个正数");
        
                m = scanner.nextInt();
                if (m < 0) {
                    System.out.println();
            
                } else {
                    
                    break;
                }
            }
    
            System.out.println("m*n = " + m*n);        }
            
            
    }
}