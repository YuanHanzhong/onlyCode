package _01_基础语法;/*
 *需求:先看视频, 要练习得代码, 用todo标记下
 *要点:
 *      1 够明天的就行
 *      2
 *      3
 */

import org.junit.Test;

public class ToDo {
    @Test
    public void test() {
        /**
         * 题目：冒泡排序
         * 要点：
         */
        int[] a = {2, 3, 4, 5, 6, 7, 8, 3, 1};
    
        for (int i = 0; i < a.length-1; i++) {
            boolean isSorted = true;
            for (int j = 0; j < a.length-1-i; j++) {
                if (a[j] < a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j+1]=temp;
                    isSorted = false;
                }
            }
            if (isSorted) {
                break;
            }
        }
        for (int i : a) {
            System.out.println(i+" ");
        }
    }
    
    @Test
    public void test2() {
        /**
         * 要点：
         */
        int[] a = {2, 3, 4, 5, 6, 7, 8, 3, 1};
        for (int i = 0; i < a.length-1; i++) {
            for (int j = 0; j < a.length-1-i; j++) {
                if(a[j]<a[j+1]){
                    int tmp = a[j];
                    a[j] = a[j+1];
                    a[j+1] = tmp;
                }
            }
        }
    
        for (int i : a) {
            System.out.print(a[i]+" ");
        }
    }
}
