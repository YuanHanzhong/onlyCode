package _07_面向对象;/*
 *需求:声明一个数组工具类ArrayTools，包含如下方法：

-   int sum(int[] arr)：求所有元素总和
-   int max(int[] arr)：求所有元素的最大值
-   int indexOf(int[] arr, int value)：查找value在arr数组中第一次出现的下标，如果不存在返回-1
-   int lastIndexOf(int[] arr, int value)：查找value在arr数组中最后一次出现的下标，如果不存在返回-1

 *要点:
 *      1
 *      2
 *      3
 */

public class ArrayTools {
    int sum(int[] arr) {
        int sum=0;
        for (int i : arr) {
            sum+=i;
        }
        return sum;
    }
    
    int max(int[] arr) {
        int max = arr[0];
        for (int i : arr) {
            if (i > max) {
                max=i;
            }
        }
        return max;
    }
    
    //-   int indexOf(int[] arr, int value)：查找value在arr数组中第一次出现的下标，如果不存在返回-1
    int indexOf(int[] arr, int value) {
        for (int i : arr) {
            if (arr[i] == value) {
                System.out.println(i);
    
                return i;
            }
        }
        return -1;
    }
    
    //  -   int lastIndexOf(int[] arr, int value)：查找value在arr数组中最后一次出现的下标，如果不存在返回-1
    int lastIndexOf(int[] arr, int value) {
    
        for (int i = arr.length-1; i >=0 ; i--) { // shuzu.length 这个值肯定是取不到的 got
            if (arr[i] == value) {
                return i;
            }
        }
        return -1;
    }
}
