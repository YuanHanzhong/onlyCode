package _04_数组.一维数组基本使用;/*
 *需求:
         已知平年12个月每个月的总天数是{ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,31}，
         从键盘输入年，月，日后，计算这一天是这一年的第几天。提示：考虑闰年
 *要点:
 *      1 闰年: 四年一闰，百年不闰，四百年在闰
 *      2 用数组比用switch好用很多
 *      3
 */

import java.util.Scanner;

public class 这一天是这一年的第几天_v3_数组实现 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Scanner 参数里右System.in
        // 不用每次都new scanner
        //
        //
        int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; // 能不妨循环里就不放循环里
        
        while (true) {
            System.out.println("年: ");
            int year = scanner.nextInt();
            System.out.println("月: ");
            int month = scanner.nextInt();
            System.out.println("日: ");
            int day = scanner.nextInt();
            
            
            int sum = 0; // got 最好定义时, 就初始化一下
            
            
            // 进行闰年处理
            if (isLunarYear(year)) {
                days[1] = 29;
            }
            
            // 1月份单独处理下
            if (month == 1) {
                sum = day;
            } else {
                for (int i = 0; i < month - 1; i++) {
                    sum = sum + day + days[i];
                }
            }
            System.out.println("第 " + sum + "天");
        }
        //scanner.close(); // 不用Scanner后要close掉
    }
    public static boolean isLunarYear(int year) {
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            System.out.println(year + "是闰年");
            return true;
        } else {
            System.out.println(year + "补是闰年");
            return false;
        }
    }
}


