package _04_数组.一维数组基本使用;/*
 *需求:
         已知平年12个月每个月的总天数是{ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,31}，
         从键盘输入年，月，日后，计算这一天是这一年的第几天。提示：考虑闰年
 *要点:
 *      1
 *      2
 *      3
 */

import java.util.Scanner;

public class 这一天是这一年的第几天_v1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Scanner 参数里右System.in
        System.out.println("年: ");
        int year = scanner.nextInt();
        System.out.println("月: ");
        int month = scanner.nextInt();
        System.out.println("日: ");
        int day = scanner.nextInt();
    
        int sum = 0; // got 最好定义时, 就初始化一下
        

        switch (month) {
            case 1:
                sum=+day;
                break;
            case 2:
                sum=sum+day+31;
                break;
    
            case 3:
                if (isLunarYear(year)) { // got 判断范围最小化, 大大减少代码量
                    sum = day + 29+31;
                    break;
                } else {
                    sum = day +28;
                    break;
                }
            case 4:
                sum=sum+day+31;
                break;
            case 5:
                sum=sum+day+30;
                break;
            case 6:
                sum=sum+day+31;
                break;
            case 7:
                sum=sum+day+30;
                break;
            case 8:
                sum=sum+day+31;
                break;
            case 9:
                sum=sum+day+31;
                break;
            case 10:
                sum=sum+day+30;
                break;
            case 11:
                sum=sum+day+31;
                break;
            case 12:
                sum=sum+day;
                break;
        }
    
        System.out.println("第 " + sum+"天");
    }

    
    public static boolean isLunarYear(int year) {
        return (year%4 == 0 && year%100 != 0) || year%400 == 0;
    }
}


//todo 让代码更优美些