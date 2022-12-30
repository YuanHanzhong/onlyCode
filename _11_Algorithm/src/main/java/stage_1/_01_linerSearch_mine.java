package stage_1;

public class _01_linerSearch_mine {
    public static void main(String[] args) {
        // 2022/11/21 23:52 NOTE P3 见名知意
        int[] arrayExamples = { 5, 6, 66, 7};
        int target = 66;
        System.out.println("search(arrayExamples, target) = " + search(arrayExamples, target));
    
    }
    
    // 2022/11/22 0:13 NOTE p3 iter 能返回索引吗
    public static int search(int[] dataset, int target) {
        for (int i = 0; i < dataset.length; i++) {
            if (dataset[i] == target) {
                return i;
            }
        }
        return -1;
    }
}
