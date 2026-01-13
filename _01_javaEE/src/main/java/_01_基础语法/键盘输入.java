package _01_基础语法;/*
 *需求:
 *要点:
 *      1 new Scanner
 *      2 scanner.nextInt
 *      3 scanner.close()
 */

import java.util.Scanner;

public class 键盘输入 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        int i = scanner.nextInt();
    
    
        System.out.println("s = " + s);
        System.out.println("i = " + i);
    
    
        scanner.close();
    }
}
