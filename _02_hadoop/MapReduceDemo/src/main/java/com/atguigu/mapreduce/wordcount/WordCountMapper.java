package com.atguigu.mapreduce.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * KEYIN, map阶段输入的key的类型：LongWritable
 * VALUEIN,map阶段输入value类型：Text
 * KEYOUT,map阶段输出的Key类型：Text
 * VALUEOUT,map阶段输出的value类型：IntWritable
 */

// 和框架对接的关键
    // <泛型>, 就是用来限制的
public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private Text outK = new Text();
    private IntWritable outV = new IntWritable(1);
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        // 1 获取一行, 偏移量是key, 行内容是value
        // atguigu atguigu
        String line = value.toString();
        // 2 切割
        // atguigu
        // atguigu
        String[] words = line.split(" ");
        // 3 循环写出
        for (String word : words) {
            // 封装outk
            outK.set(word);

            // 写出
            context.write(outK, outV); //(a,1),(a,1)(b,1)
        }
    }
}
