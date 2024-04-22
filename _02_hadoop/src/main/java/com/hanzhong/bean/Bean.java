package com.hanzhong.bean;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Bean implements Writable {
    
    private long up;
    private long down;
    private long sum;
    
    
    // 2024/04/22 022 下午 08:19 NOTE 重载下必要的方法
    public void setSum() {
        this.sum = this.up + this.down;
    }
    
    // 2024/04/22 022 下午 08:12 NOTE 空参构造, Hadoop 在反序列化时,使用反射机制创建对象的实例。
    public Bean() {
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(up);
        out.writeLong(down);
        out.writeLong(sum);
    }
    
    @Override
    public void readFields(DataInput in) throws IOException {
        // 2024/04/22 022 下午 08:25 NOTE up down sum顺序和上面对应
        this.up = in.readLong();
        this.down = in.readLong();
        this.sum = in.readLong();
    }
    
    @Override
    public String toString() {
        return up + "\t" + down + "\t" + sum;
    }
    
    // 2024/04/22 022 下午 08:20 NOTE alt+insert 自动生成的, 把光标放到最后
    
    public long getUp() {
        return up;
    }
    
    public void setUp(long up) {
        this.up = up;
    }
    
    public long getDown() {
        return down;
    }
    
    public void setDown(long down) {
        this.down = down;
    }
    
    public long getSum() {
        return sum;
    }
    
    public void setSum(long sum) {
        this.sum = sum;
    }
}
