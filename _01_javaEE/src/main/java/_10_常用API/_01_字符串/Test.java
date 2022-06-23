package _10_常用API._01_字符串;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import java.util.ArrayList;
import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("hello");
        strings.add(" Jack");
    
        strings.addAll(strings);
    
        String s = strings.get(2);
        ArrayList<String> strings1 = new ArrayList<>();
        strings1.add(s);
        strings1.add("be ");
    
        System.out.println("s = " + s);
    
        int size = strings.size();
    
        System.out.println("size = " + size);
    
        Object[] objects = strings.toArray();
        System.out.println("Arrays.toString(objects) = " + Arrays.toString(objects));
    
        System.out.println("strings.containsAll(s) = " + strings.containsAll(strings1));
    
    }
}
