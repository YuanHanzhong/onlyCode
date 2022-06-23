package _04_数组.一维数组基本使用;/*
 *需求:
 用一个数组，保存星期一到星期天的7个英语单词，从键盘输入1-7，显示对应的单词
 *要点:
 *      1 定义数组, 没有确定值时, 要new 一下
 *      2 while(true) 不断接受输入值
 *      3 对输入健壮性过滤
 */

import java.util.Scanner;

public class 星期 {
    public static void main(String[] args) {
        String[] week = {"Monday","T","W","Th","Fri","Sat","Sun"};
        
        // star 数组和对象, 在用之前, 都是需要显示 new()
        Scanner scanner = new Scanner(System.in);
        while (true) {
            int input = scanner.nextInt();
            
            // 健壮, 对输入过滤
            if (input < 7 && input >= 0) {
                System.out.println(week[input]);
        
            } else {
                System.out.println("请输入0-6的数字");
            }
        }
    }
}
