package _03_流程控制.下;/*
 *需求:
 ---------ATM-------
	1、存款
	2、取款
	3、显示余额
	4、退出

 */

import java.util.Scanner;

public class 第5题_ATM {
    public static void main(String[] args) {
        double yue = 0; // done 怎么起名字 balance标识余额
        double amountMoney = 0; // done 非要初始化, 就先初始化为0;
    
        while (true) {
            System.out.println("请选择: " + // done 直接粘贴, 很完美的格式
                                       " ---------ATM-------\n" +
                                       "\t1、存款\n" +
                                       "\t2、取款\n" +
                                       "\t3、显示余额\n" +
                                       "\t4、退出");
    
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
    
            if (choice == 1 || choice == 2) {
                System.out.println("请输入金额: ");
                amountMoney = scanner.nextDouble(); // done 只有1和2的时候, 才需要输入, 才需要读取
            }
            switch (choice) {
                case 1:
                    yue += amountMoney;
                    System.out.println("存款");
                    System.out.println("yue = " + yue);
                    break;
                case 2:
                    yue -= amountMoney;
                    System.out.println("取款");
                    System.out.println("yue = " + yue);
                    break;
                case 3:
                    System.out.println("显示金额");
                    System.out.println("yue = " + yue);
                    break;
                case 4:
                    return; // got 整个退出, 就是return, 没有错
            }
        }
    }
}
