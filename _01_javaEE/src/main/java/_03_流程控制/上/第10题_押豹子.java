package _03_流程控制.上;/*
 *需求:
 10、赌数游戏：随机产生3个1-6的整数，如果三个数相等，那么称为“豹子”，如果三个数之和大于9，称为“豹子”，如果三个数之和小于等于9，称为“小”，用户从键盘输入押的是“王”、“大”、“小”，并判断是否猜对了

提示：随机数  Math.random()产生 [0,1)范围内的小数

 *要点:
 *      1
 *      2
 *      3
 */

public class 第10题_押豹子 {
    public static void main(String[] args) {
        int[] ints = new int[3];
        for (int i = 0; i < 3; i++) {
            int a=(int)(Math.random()*5+1) ; // got 倒数第2步加, 最后1步强转
            System.out.println("a = " + a);
        
        }
    
    }
    
    

}
