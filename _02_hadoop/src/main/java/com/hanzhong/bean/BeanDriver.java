package com.hanzhong.bean;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

// 打断点
// 关心哪里点在哪里
public class BeanDriver {
    
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        
        // 1. 获取job,
        //      只需要job.get, 其他按提示. 或者找示例.
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);
        
        // 2. 设置jar包路径,
        //      后续程序会自己通过反射获取路径
        job.setJarByClass(BeanDriver.class);
        
        // 3. 关联mapper, reducer
        job.setMapperClass(BeanMapper.class);
        job.setReducerClass(BeanReducer.class);
        
        // 4. 设置map类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Bean.class);
        
        // 5. 设置最终输出类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Bean.class);
        
        // 6. 设置输入输出路径
        // 放到集群上的时候,
        FileInputFormat.setInputPaths(job, new Path("D:\\input\\inputflow"));
        FileOutputFormat.setOutputPath(job, new Path("D:\\hadoop\\bean99"));
        
        // 7. 提交
        boolean result = job.waitForCompletion(true);
        
        System.exit(result ? 0 : 1);
    }
    
}
