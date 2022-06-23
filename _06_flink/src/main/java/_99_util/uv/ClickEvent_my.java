package _99_util.uv;

import java.sql.Timestamp;

/*
需求: 模拟点击事件
分析:
    需要知道
        唯一标识, username
        做了什么, url
        时间戳 ts
            ts 一般定义成Long
            为了方便看, 常用sql.Timestamp转化下, 通过new的方式
got
    POJO 只负责最基本的, 不写其他的逻辑
 */
public class ClickEvent_my {
    public String username;
    public String url;
    public Long ts;
    
    @Override
    public String toString() {
        return "ClickEvent_my{" +
                 "username='" + username + '\'' +
                 ", url='" + url + '\'' +
                 ", ts=" + new Timestamp(ts) +
                 '}';
    }
    
    public ClickEvent_my(String username, String url, Long ts) {
        this.username = username;
        this.url = url;
        this.ts = ts;
    }
    
    public ClickEvent_my() {
    }
}
