package com.hanzhong.bean;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class BeanReducer extends Reducer<Text, Bean, Text, Bean> {
    
    // 2024/04/22 022 下午 09:24 NOTE 得有new
    private Bean outValue = new Bean();
    
    
    @Override
    protected void reduce(Text key, Iterable<Bean> values, Reducer<Text, Bean, Text, Bean>.Context context) throws IOException, InterruptedException {
        // 下面这一行, 有什么作用?
        //super.reduce(key, values, context);
        
        // 2024/04/22 022 下午 09:29 NOTE  因为是以手机号为单位统计的
        long amountUp = 0;
        long amountDown = 0;
        // sum 不用写, 自动就计算好了
        //private long amountSum = 0;
        
        for (Bean value : values) {
            amountUp += value.getUp();
            amountDown += value.getDown();
        }
        // 2024/04/22 022 下午 09:19 NOTE 能放循环外就放循环外
        outValue.setUp(amountUp);
        outValue.setDown(amountDown);
        outValue.setSum();
        
        context.write(key, outValue);
    }
}
