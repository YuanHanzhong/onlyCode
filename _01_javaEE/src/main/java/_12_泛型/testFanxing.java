package _12_泛型;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;

public class testFanxing {
    public static void main(String[] args) {
    
        // 定义对象数组
        Rec[] recs = new Rec[5];
        recs[0] = new Rec(2, 6);
        recs[1] = new Rec(3, 4);
        recs[2] = new Rec(12, 1);
        recs[3] = new Rec(5, 4);
        recs[4] = new Rec(5, 1);
    
        System.out.println("先面积, 后周长:\n");
        Arrays.sort(recs);
    
        
        for (Rec rec : recs) {
            System.out.println("rec = " + rec);
        }
        
        
        // 比较面积
        System.out.println("only area \n");
        Arrays.sort(recs, new Comparator<Rec>() {
            @Override
            public int compare(Rec o1, Rec o2) {
                // 比较两个都变了时, 不能直接详见
                return Double.compare(o1.perimeter(),o2.perimeter());
            }
    
        });
        for (Rec rec : recs) {
            System.out.println("rec = " + rec);
        }
    }
}
