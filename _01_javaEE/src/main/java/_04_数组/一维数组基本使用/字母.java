package _04_数组.一维数组基本使用;/*
 *需求:
 *要点:
 *      1 for循环数组, 若从0开始, 那么小于n, 就循环n次
 *      2 char+int 结果为int, 若要输出字符, 需要(char)
 *      3 不用算长度, 用length, 省事还优美
 4> 用tab 提示, 事半功倍
 
 */

public class 字母 {
    public static void main(String[] args) {
        
        char[] chars = new char[26];
        
        for (int i = 0; i < chars.length; i++) { // got for循环数组, 若从0开始, 那么小于n, 就循环n次
            chars[i] = (char) (i + 97);
    
        }
        for (int i = 0; i < chars.length; i++) { // got 想要全部循环, 根本不需要算, 直接用 length就可
            System.out.println("大写 " + (char)(chars[i] -32));
        
        }
        for (int i = 0; i < chars.length; i++) {
            System.out.println("小写  " + chars[i]);
        
        }
    }
}
