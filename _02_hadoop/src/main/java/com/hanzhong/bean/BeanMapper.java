package com.hanzhong.bean;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;


// 2024/04/22 022 下午 08:50 NOTE 对数据源的分析非常重要, 直接影响了程序
//1	13736230513	192.196.100.1	www.atguigu.com	2481	24681	200
//2	13846544121	192.196.100.2					264		0		200
//3 	13956435636	192.196.100.3				132		1512	200
//4 	13846544121	192.168.100.1				240		0		404

public class BeanMapper extends Mapper<LongWritable, Text, Text, Bean> {
    
    private Text outKey = new Text();
    private Bean outValue = new Bean();
    
    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, Bean>.Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String[] splits = line.split("\t");
        
        // 2024/04/22 022 下午 09:43 P3 声明变量的地方很有区别
        String phone = splits[1];
        // 2024/04/22 022 下午 08:58 NOTE 长度, 直接举例子比较好
        
        // 2024/04/22 022 下午 09:40 NOTE 排查bug, 不但看caused by, 也要点击链接
        String up = splits[splits.length - 3];
        String down = splits[splits.length - 2];
        
        // 2024/04/22 022 下午 09:00 NOTE 封装, 分步骤, 有节奏, 不容易乱
        outKey.set(phone);
        
        // 2024/04/22 022 下午 09:02 NOTE 把字符串转化为Long
        outValue.setUp(Long.parseLong(up));
        outValue.setDown(Long.parseLong(down));
        
        // 不用再解析了, 这就是巧妙之处. up 和 down都还在内存中
        outValue.setSum();
        
        context.write(outKey, outValue);
        
    }
}
