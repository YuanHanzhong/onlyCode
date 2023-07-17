package _01_tools;

import java.sql.Timestamp;

public class POJO_ClickEvent {
    public String url;
    public String name;
    public Long timestamp;
    
    @Override
    public String toString() {
        return "ClickEvent{" +
                 "url='" + url + '\'' +
                 ", name='" + name + '\'' +
                 ", timestamp=" + new Timestamp(timestamp) +
                 '}';
    }
    
    public POJO_ClickEvent(String url, String name, Long timestamp) {
        this.url = url;
        this.name = name;
        this.timestamp = timestamp;
    }
    
    public POJO_ClickEvent() {
    }
}
