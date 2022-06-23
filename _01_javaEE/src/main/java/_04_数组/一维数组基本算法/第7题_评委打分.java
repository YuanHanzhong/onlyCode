package _04_数组.一维数组基本算法;/*
 *需求: 有10个评委给参加编程比赛的某个选手的作品打分，从键盘输入10个评委的分数，并存储到一维数组中。然后求选手的最后得分（去掉一个最高分和一个最低分后其余8位评委打分的平均值）
 *要点:
 *      1
 *      2
 *      3
 */

import java.util.Scanner;

public class 第7题_评委打分 {
    public static void main(String[] args) {
        int[] a = new int[5];
        Scanner scanner = new Scanner(System.in);
    
        System.out.println("请输入成绩: ");
        for (int i = 0; i < a.length; i++) {
            a[i]=scanner.nextInt();
            
        }
        scanner.close();
    
    
        // 冒泡排序
        for (int i = 0; i < a.length - 1; i++) {
            boolean isSorted = true;
            for (int j = 0; j < a.length - 1 - i; j++) {
                if (a[j] < a[j + 1]) {
                    int temp= a[j];
                    a[j] = a[j + 1];
                    a[j+1] = temp;
                    isSorted = false;
                }
            }
            if (isSorted) {
                break;
            }
        }
    
        for (int i : a) {
            System.out.println("i = " + i);
        }
    
        System.out.println("最高分: "+a[0]);
        System.out.println("最低分: "+a[a.length-1]);
        
        // 中间8个平均分
        int sum=0;
        for (int i = 1; i < a.length - 1; i++) { // got 小于 length , 那么小标对应的是length-1, length-1 就代表最后一个
            sum += a[i];
        }
        double avg = sum*1.0 / (a.length - 2);
    
        System.out.println("平均分为 = " + avg);
    }
    
}
