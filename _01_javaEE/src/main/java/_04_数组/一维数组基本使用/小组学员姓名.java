package _04_数组.一维数组基本使用;/*
 *需求:
 用一个数组存储本组学员的姓名，先从键盘输入小组人数，再从键盘输入每一个学员的姓名，然后遍历显示。
 *要点:
 *      1
 *      2
 *      3
 */

import java.util.Scanner;

public class 小组学员姓名 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("几个人: ");
    
        String[] group = new String[scanner.nextInt()];
    
        for (int i = 0; i < group.length; i++) {
            System.out.println("请输入姓名, 回车输入下一个姓名");
            group[i] = scanner.next();
            // 用 nextLine 可以接受中间有空格的, nextLine每次输入都会吃掉一个回车.
            // got scanner.next(), 遇到空格就算1个
        }
        for (String s : group) {
            System.out.println(s);
        }
    }
}
