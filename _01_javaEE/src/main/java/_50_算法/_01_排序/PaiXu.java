package _50_算法._01_排序;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import org.junit.Test;

public class PaiXu {
    
    @Test
    public void test1() {
        /**
         * 题目：冒泡排序基本
         * 要点：
         *      1> 3个元素, 比较2趟即可
         *          1.1> -1 抵消从0开始
         *          1.2> < 再减少一个元素
         *      2> 3个元素, 比较2次即可
         *      3> 比较的是相邻的2个元素, 只与内循环有关,
         *      4> 外循环的i只是做个标记而已
         *      分析边界值很关键 got
         *
         */
        int a[] = {3, 1, 2, 3};
        int temp = 0;
        for (int i = 0; i < a.length - 1; i++) { // i 的范围[0, a.length-2], 外循环length-1次
            // i的最大值是 a.length - 2, array[a.length-2]是数组中的倒数第二个元素 got
            for (int j = 0; j < a.length - 1 - i; j++) { // [0, length-2], 0 对应的是第一个元素, length-2 对应的是倒数第二个元素
                if (a[j] > a[j + 1]) {
                    temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }
        for (int i : a) {
            System.out.println("i = " + i);
        }
    }
    
    @Test
    public void test2() {
        /**
         * 题目：冒泡排序升级
         * 要点：
         *      1> 找到排序的边界 sortBorder, 左边有序, 右边无序
         *      2> 一旦有一次不排, 后面就都不排了
         */
        int array[] = {33, 21, 3, 4, 77, 2, 2};
        int Boundary = array.length;
        int temp = 0;
        for (int i = 0; i < Boundary - 1; i++) {
            boolean isSorted = true;
            
            for (int j = 0; j < Boundary - i - 1; j++) {
                if (array[j] < array[j + 1]) {
                    temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    isSorted = false; // 除非他执行, 否则就会break, 每一趟都会确认下
                }
            }
            if (isSorted) {
                break;
            }
        }
        
        for (int i : array) {
            System.out.println("i = " + i);
        }
    }
    
    @Test
    public void test3() {
        /**
         * 题目：演练冒泡排序
         * 要点：
         */
        int array[] = {2, 4, 6, 1};
        
        // 设置临时变量
        int temp;
        
        // 外循环, 只需要元素个数-1趟
        for (int i = 0; i < array.length - 1; i++) {
            // 设置flag, 为高效
            boolean isSorted = true;
            for (int j = 0; j < array.length - 1 - i; j++) { // 前面比过了, 无需再比
                // 如果小, 则交换
                if (array[j] < array[j + 1]) {
                    // 交换
                    temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    // 如果一次也不交换, 那下一趟就没有必要再比了
                    isSorted = false; // 抓住机会告诉外边, 剩下的无序, 还需要比. 如果不说, 那就结束了.
                }
                
            }
            // 判断有无必要再来一趟
            if (isSorted) {
                //break;
            }
        }
        for (int i : array) {
            System.out.print(i + " "); // 输出的时候, 不能让 int + '', 因为自动强转为数值了
        }
    }
    
    @Test
    public void test4() {
        
        /**
         * 题目：冒泡排序再练习
         * 要点：
         */
        int a[] = {2, 1, 3, 4, 9, 7, 5};
        int temp;
        // 排序
        // 外排
        for (int i = 0; i < a.length - 1; i++) {
            // 内排
            // 定义flag, 不是所有的外排都需要
            boolean isSorted = true;
            for (int j = 0; j < a.length - 1 - i; j++) {
                // 如果小, 则交换
                if (a[j] < a[j + 1]) {
                    temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                    isSorted = false;
                }
                
                
            }
            if (isSorted) {
                break;
            }
        }
        
        // 输出
        for (int i = 0; i < a.length; i++) {
            System.out.println("i = " + a[i]);
        }
        
        
    }
    
    @Test
    public void test8() {
        /**
         * 题目：冒泡练习
         * 要点：
         */
    
        int[] a = {3, 2, 5, 8, 9, 5};
        // 外循环
        for (int i = 0; i < a.length-1; i++) { //[0, a.length-2]
            // 标记下是否有序
            boolean isSorted = true;
            //内循环
            for (int j = 0; j < a.length-1-i; j++) {
                if (a[j] < a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                    isSorted = false;
                }
                
            }
            if (isSorted) {
                break;
            }
        }
        // iter 直接输出内容, 自己手写的循环, 要用下标
        for (int i : a) {
            System.out.println("i = " + i);
        }
    }
  
    
    
}

/*
1、随机产生10个[0,100)的偶数，并按照从大到小的顺序排列。
（可以使用冒泡排序或直接选择排序）
 */
class Exam1 {
    public static void main(String[] args) {
        int[] arr = new int[10];
        
        System.out.println("排序之前：");
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int)(Math.random()*50)*2;//[0,50)*2==>[0,100)的偶数
            System.out.print(arr[i]+" ");
        }
        System.out.println();
        
        //排序：冒泡排序
        for (int i = 1; i < arr.length; i++) {// [1, length-1], 循环length-1次
            for (int j = 0; j < arr.length-i; j++) {// [0, length-2]
                if(arr[j] < arr[j+1]){
                    int temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                }
            }
        }
        
        //排序后显示结果
        System.out.println("排序后：");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] +" ");
        }
        System.out.println();
        
          
    }
}
