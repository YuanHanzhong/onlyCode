package _99_util._1_topN;



import java.sql.Timestamp;

/*
需求: 商品在窗口中的计数,
    需要以下字段, 有需求决定
        productID;
        count
        windowStartTime
        windowEndTime
    时间为北京时间
    
备注:
    1分40秒
    51秒
    41秒
 */

public class ProductViewCountPerWindow_my {
    public String productID;
    public Long count;
    public Long windowStartTime;
    public Long windowEndTime;
    
    @Override
    public String toString() {
        return "ProductViewCountPerWindow_my{" +
                 "productID='" + productID + '\'' +
                 ", count=" + count +
                 ", windowStartTime=" + windowStartTime +
                 ", windowEndTime=" + windowEndTime +
                 '}';
    }
    
    public ProductViewCountPerWindow_my(String productID, Long count, Long windowStartTime, Long windowEndTime) {
        this.productID = productID;
        this.count = count;
        this.windowStartTime = windowStartTime;
        this.windowEndTime = windowEndTime;
    }
    
    public ProductViewCountPerWindow_my() {
    }
}
