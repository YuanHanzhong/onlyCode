package org;

public class MaoPao {
    public static void main(String[] args) {
        
        int[] a = {2, 3, 4, 5, 6, 7, 8, 3, 1};
        
        // 测试快速排序
        quickSort(a, 0, a.length - 1);
        for (int i : a) {
            System.out.println(i);
        }
    }
    
    // 快速排序
    public static void quickSort(int[] a, int low, int high) {
        if (low < high) {
            int pivot = partition(a, low, high);
            
            quickSort(a, low, pivot - 1);
        }
    }
    
    private static int partition(int[] a, int low, int high) {
        int pivot = a[low];
        while (low < high) {
            while (low < high && a[high] >= pivot) {
                high--;
            }
            a[low] = a[high];
            while (low < high && a[low] <= pivot) {
                low++;
            }
            a[high] = a[low];
        }
        a[low] = pivot;
        return pivot;
    }
    
    
}
