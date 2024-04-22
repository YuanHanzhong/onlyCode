package com.hanzhong.mrServer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

// 打断点
// 关心哪里点在哪里
public class WCDriver {
    
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        
        // 1. 获取job,
        //      只需要job.get, 其他按提示. 或者找示例.
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);
        
        // 2. 设置jar包路径,
        //      后续程序会自己通过反射获取路径
        job.setJarByClass(WCDriver.class);
        
        // 3. 关联mapper, reducer
        job.setMapperClass(WCMapper.class);
        job.setReducerClass(WCReducer.class);
        
        // 4. 设置map类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        
        // 5. 设置最终输出类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        
        // 6. 设置输入输出路径
        // 放到集群上的时候, 需要改参数
        // 使用调度工具调度脚本
        // hadoop jar _02_hadoop-1.0-SNAPSHOT.jar com.hanzhong.mrServer.WCDriver  /hadoopTest/input /hadoopTest/output
        // 2024/04/22 022 上午 09:08 NOTE  com.hanzhong.mrServer.WCDriver 是到代码中, 右键复制的全类名
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        
        // 7. 提交
        boolean result = job.waitForCompletion(true);
        
        System.exit(result ? 0 : 1);
    }
    
}
