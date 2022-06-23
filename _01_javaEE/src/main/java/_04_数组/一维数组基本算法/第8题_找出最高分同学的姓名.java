package _04_数组.一维数组基本算法;/*
 *需求: 先从键盘输入小组人数，然后声明两个数组，一个存储本组学员姓名，一个存储本组学员成绩，再从键盘输入每一个学员的姓名和成绩，遍历显示小组中每一个学员的姓名和分数。最后显示最高分同学的姓名和成绩。
 *要点:
 *      1
 *      2
 *      3
 */

import java.util.Scanner;

public class 第8题_找出最高分同学的姓名 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入人数");
        int count=scanner.nextInt();
        int[] scores = new int[count];
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            System.out.println("第"+(i+1)+"个同学姓名: ");
            names[i] = scanner.next(); // println 不和 nextLine连用 got
            System.out.println("第"+(i+1)+"个同学分数: ");
            scores[i] = scanner.nextInt();
        }
    
        for (int i = 0; i < count; i++) {
            System.out.println("names = " + names[i]+"  分数"+scores[i]);
        }
        
        //Max
        int max = scores[0];
        int flag=0; // 一定义, 能初始化就初始化
        for (int i = 0; i < count; i++) {
            if (max < scores[i]) {
                max = scores[i];
                flag = i;
            }
        }
    
        System.out.println("最高分数同学的names = " + names[flag]+"分数为: "+scores[flag]);
        
    
        scanner.close();
    }
}
