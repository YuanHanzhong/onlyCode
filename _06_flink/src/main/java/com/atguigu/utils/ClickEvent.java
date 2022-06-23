package com.atguigu.utils;

import java.sql.Timestamp;

public class ClickEvent {
    
    
    public String username;
    public String url;
    public Long ts;
    
    @Override
    public String toString() {
        return "ClickEvent{" +
                 "usrname='" + username + '\'' +
                 ", url='" + url + '\'' +
                 ", ts=" + new Timestamp(ts) + // 把传进来的的时间转化为了timestamp
                 '}';
    }
    
    public ClickEvent(String username, String url, Long ts) {
        this.username = username;
        this.url = url;
        this.ts = ts;
    }
    
    public ClickEvent() {
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}
