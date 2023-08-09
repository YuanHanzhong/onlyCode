package com.atguigu.bigdata.spark.core.test;

import java.util.*;

public class TestMapMerge {
    public static void main(String[] args) {

        Map<String, Integer> map1 = new HashMap<String, Integer>();
        map1.put("a", 2);
        map1.put("b", 3);
        map1.put("c", 1);

        Map<String, Integer> map2 = new HashMap<String, Integer>();
        map2.put("a", 4);
        map2.put("b", 1);
        map2.put("d", 5);

        // ((a, 6), (b, 4), (c, 1), (d, 5))
        // TODO 两个Map合并时，基本原则就是以一个Map为中心进行更新(添加)
        final Iterator<String> keyIter = map2.keySet().iterator();
        while ( keyIter.hasNext() ) {
            final String map2Key = keyIter.next();
            final Integer map2Value = map2.get(map2Key);
            final Integer map1Value = map1.get(map2Key);
            if ( map1Value == null ) {
                map1.put(map2Key, map2Value);
            } else {
                map1.put(map2Key, map1Value + map2Value);
            }
        }
        System.out.println(map1);

    }
}
