package _05;

/**
 * pojo 的标准写法是什么 STAR
 *      0. 字段全部为 public
 *      1. 无参
 *      2. 全参
 *      3. to string
 */
public class _01_EventPojo {
    public String user;
    public String url;
    public Long timestamp;
    
    
    public _01_EventPojo() {
    }
    
    @Override
    public String toString() {
        return "EventPojo{" +
                 "user='" + user + '\'' +
                 ", url='" + url + '\'' +
                 ", timestamp=" + timestamp +
                 '}';
    }
    
    public _01_EventPojo(String user, String url, Long timestamp) {
        this.user = user;
        this.url = url;
        this.timestamp = timestamp;
    }
}
