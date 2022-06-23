package _03_流程控制.下;/*
 *需求: 所谓水仙花数是指一个3位数，其各个位上数字立方和等于其本身。例如： 153 = 1*1*1 + 5*5*5 + 3*3*3，找出所有的水仙花数，并统计他们有几个。
 *要点:
 *      1
 *      2
 *      3
 */

public class 第2题_水仙花数 {
    public static void main(String[] args) {
        int amount = 0;
        
        for (int i = 0; i < 1000; i++) {
            int ge = i%10;
            int shi = i%100/10;
            int bai = i/100;
    
            if (i == ge * ge * ge + shi * shi * shi + bai*bai*bai) {
                amount++;
                System.out.println(i);
            }
        }
        System.out.println("amount = " + amount);
        
    }
}
