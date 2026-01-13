package _01_基础语法;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class shua {
    public static void main(String[] args) {
        int i = 0;
        System.out.println("change(i) = " + change(i));
        System.out.println("change(i) = " + change(change(i)));
        ++i;
        System.out.println("i = " + i);
    }
    public static int change(int i){
        i = i+10;
        return i;
    }
}
