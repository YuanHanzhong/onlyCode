package com.hanzhong.mrServer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;


public class WCMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    
    // 一般new的时候, 不放到循环里 P3 2024年4月21日
    private Text outKey = new Text();
    // 直接在类下面的时候, 一般设置为private, 安全, 方便维护. P3 2024年4月21日
    private IntWritable outValue = new IntWritable();
    
    @Override
    // 每一行都要调用一次map
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        String line = value.toString();
        // .var 或者 alt+enter 补全很好用 P3 2024年4月21日
        // 这里用双引 是String, 单引是char P3 2024年4月21日
        String[] words = line.split(" ");
        
        for (String word : words) {
            outKey.set(word);
            outValue.set(1);
            context.write(outKey, outValue);
        }
    }
}
