package _12_泛型._1_;

import java.util.ArrayList;
import java.util.function.Predicate;

public class test {
    public static void main(String[] args) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("helloolleh");
        strings.add("java");
        strings.add("world");
        strings.add("atguigu");
        strings.add(("noon"));
    
        System.out.println("init: ");
        for (String string : strings) {
            System.out.println("string = " + string);
        }
        
        strings.removeIf(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                if (Huiwen.isHuiwen(s)) {
                    return true;
                }
                else return false;
            }
        });
    
        System.out.println("remove huiwen");
        for (String string : strings) {
            System.out.println("string = " + string);
        }
    }
    
}
