package _99_util._1_topN;


import java.sql.Timestamp;

/*
需求: UserBehavior POJO 需要以下字段(由源数据决定)

userID
productID
categoryID
type
ts

备注:
    1分54秒
    1分18秒
    1分10秒
    1分11秒
    56秒
    

 */


public class UserBehavior_my {
    public  String userID;
    public String productID;
    public String categoryID;
    public String type;
    public Long ts;
    
    @Override
    public String toString() {
        return "UserBehavior_my{" +
                 "userID='" + userID + '\'' +
                 ", productID='" + productID + '\'' +
                 ", categoryID='" + categoryID + '\'' +
                 ", type='" + type + '\'' +
                 ", ts=" + ts +
                 '}';
    }
    
    public UserBehavior_my(String userID, String productID, String categoryID, String type, Long ts) {
        this.userID = userID;
        this.productID = productID;
        this.categoryID = categoryID;
        this.type = type;
        this.ts = ts;
    }
    
    public UserBehavior_my() {
    }
}
