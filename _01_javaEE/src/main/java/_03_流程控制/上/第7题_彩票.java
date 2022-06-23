package _03_流程控制.上;/*
 *需求:
        7、假设你想开发一个玩彩票的游戏，程序随机地产生一个两位数的彩票，提示用户输入一个两位数，然后按照下面的规则判定用户是否能赢。
            
            1)如果用户输入的数匹配彩票的实际顺序，奖金10 000美元。
            
            2)如果用户输入的所有数字匹配彩票的所有数字，但顺序不一致，奖金 3 000美元。
            
            3)如果用户输入的一个数字仅满足顺序情况下匹配彩票的一个数字，奖金1 000美元。
            
            4)如果用户输入的一个数字仅满足非顺序情况下匹配彩票的一个数字，奖金500美元。
            
            5)如果用户输入的数字没有匹配任何一个数字，则彩票作废。

 *要点:
 *      1 got Math.random() 最后强转, 先乘或加, 再转换类型
 *      2 got else if 不重不漏
 *      3
 */

import java.util.Scanner;

public class 第7题_彩票 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // new 最好不放在循环里
    
        while (true) {
    
            int number = (int) (Math.random() * 90 + 10);
            System.out.println("number = " + number);
    
            System.out.println("输入2位数");
            int inputNumber = scanner.nextInt();
    
            // 拆分数字
            int number_shi = number/10;
            int number_ge = number%10;
    
            int inputNumber_shi = inputNumber/10;
            int inputNumber_ge = inputNumber%10;
    
            //1)如果用户输入的数匹配彩票的实际顺序，奖金10 000美元。
            if (number == inputNumber) {
                System.out.println("完全正确");
            }
            //
            //2)如果用户输入的所有数字匹配彩票的所有数字，但顺序不一致，奖金 3 000美元。
            //
            else if (number_shi == inputNumber_ge && number_ge == inputNumber_shi) {
                System.out.println("匹配彩票的所有数字，但顺序不一致");
            }
            //3)如果用户输入的一个数字仅满足顺序情况下匹配彩票的一个数字，奖金1 000美元。
            //
            else if (inputNumber_shi == number_shi || inputNumber_ge == number_ge) {
                System.out.println("顺序情况下匹配彩票的一个数字");
            }
            //4)如果用户输入的一个数字仅满足非顺序情况下匹配彩票的一个数字，奖金500美元。
            //
            else if (inputNumber_shi == number_ge || inputNumber_ge == number_shi) {
                System.out.println("满足非顺序情况下匹配彩票的一个数字");
            }
            //5)如果用户输入的数字没有匹配任何一个数字，则彩票作废
            else {
                System.out.println("没有匹配任何一个数字，则彩票作废");
            }
        }
    
    }
}
