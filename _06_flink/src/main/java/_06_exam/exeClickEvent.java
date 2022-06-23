package _06_exam;

public class exeClickEvent {
    public String username;
    public String url;
    
    public Long ts;
    
    @Override
    public String toString() {
        return "ClickEvent{" +
                 "username='" + username + '\'' +
                 ", url='" + url + '\'' +
                 ", ts=" + ts +
                 '}';
    }
    
    public exeClickEvent(String username, String url, Long ts) {
        this.username = username;
        this.url = url;
        this.ts = ts;
    }
    
    public exeClickEvent() {
    }
}
