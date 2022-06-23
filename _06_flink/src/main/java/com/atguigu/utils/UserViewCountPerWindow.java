package com.atguigu.utils;

import java.sql.Timestamp;

public class UserViewCountPerWindow {
    public String username;
    public Long count;
    public Long windowStartTime;
    public Long windowEndTime;
    
    public UserViewCountPerWindow() {
    }
    
    public UserViewCountPerWindow(String username, Long count, Long windowStartTime, Long windowEndTime) {
        this.username = username;
        this.count = count;
        this.windowStartTime = windowStartTime;
        this.windowEndTime = windowEndTime;
    }
    
    @Override
    public String toString() {
        return "(" +
                 "" + username +
                 ", " + count +
                 ", " + new Timestamp(windowStartTime) +
                 "~" + new Timestamp(windowEndTime) +
                 ')';
    }
}
