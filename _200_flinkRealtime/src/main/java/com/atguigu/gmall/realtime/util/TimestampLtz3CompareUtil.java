package com.atguigu.gmall.realtime.util;

/**
 * Desc: FlinkSQL时间类型TimestampLtz3比较工具类
 * 字符串的比较
 */
public class TimestampLtz3CompareUtil {
    public static int compare(String timestamp1, String timestamp2) {
        // 数据格式 2022-04-01 10:20:47.302Z
        // 1. 去除末尾的时区标志，'Z' 表示 0 时区, 很有必要.
        String cleanedTime1 = timestamp1.substring(0, timestamp1.length() - 1);
        String cleanedTime2 = timestamp2.substring(0, timestamp2.length() - 1);
        // 2. 比较时间
        return cleanedTime1.compareTo(cleanedTime2);
    }

    public static void main(String[] args) {
        System.out.println(compare("2022-04-01 11:10:55.040Z",
            "2022-04-01 11:10:55.04Z"));
    
        System.out.println("\"2022-04-01 11:10:55.040Z\".compareTo(\"2022-04-01 11:10:55.04Z\") = " + "2022-04-01 11:10:55.040".compareTo("2022-04-01 11:10:55.041"));
        System.out.println("\"2022-04-01 11:10:55.040Z\".compareTo(\"2022-04-01 11:10:55.04Z\") = " + "2022-04-01 11:10:55.050".compareTo("2022-04-01 11:10:55.04Z"));
        System.out.println("\"2022-04-01 11:10:55.040Z\".compareTo(\"2022-04-01 11:10:55.04Z\") = " + "2022-04-01 11:10:55.040".compareTo("2022-04-01 11:10:55.031"));
        System.out.println("\"2022-04-01 11:10:55.040Z\".compareTo(\"2022-04-01 11:10:55.04Z\") = " + "2022-04-01 11:10:55.040".compareTo("2022-04-00 11:10:55.041"));
    }
}
