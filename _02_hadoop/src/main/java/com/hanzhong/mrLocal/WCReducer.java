package com.hanzhong.mrLocal;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class WCReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable valueOut = new IntWritable();
    
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        int sum = 0;
        // a, (1,1)
        for (IntWritable value : values) {
            sum += value.get();
        }
        valueOut.set(sum);
        context.write(key, valueOut);
    }
}
