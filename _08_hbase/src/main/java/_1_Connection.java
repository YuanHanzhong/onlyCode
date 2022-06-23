import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class _1_Connection {
    // 工具类就是先用static修饰
    static Connection connection;  // 定义了个全局的
    
    // 实用静态代码块
    static {
        Configuration configuration = new Configuration();
        //Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "hadoop102,hadoop103,hadoop104");
        try {
            connection = ConnectionFactory.createConnection(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println("connection = " + connection);
        createTable(null,"code2","info1","info2"); // null和""不一样, "" 不报错, null会报错
        
    }
    
    // DDL
    public static void createTable(String namespaceName, String tableName,String ... cfs) throws IOException { // 必须要先有namespace
    
        // 1. 锦上添花, 更健壮
        if (namespaceName == null) {
            namespaceName = "default";
        }
        
        // 2. 获取表名
        TableName tn = TableName.valueOf(namespaceName, tableName);
    
        Admin admin = connection.getAdmin();
        
        // 2. 操作表前, 先判断
        boolean exists = admin.tableExists(tn);
        if (exists) {
            System.out.println(namespaceName+":"+tableName+"  表存在");
            return;
        }
        
        // 3. 建表
    
    
    
        
        
        TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(tn);
    
        for (String cf : cfs) {
        
            ColumnFamilyDescriptor familyDescriptor = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(cf)).build();
            tableDescriptorBuilder.setColumnFamily(familyDescriptor);
        
        }
        
        
        TableDescriptor tableDescriptor = tableDescriptorBuilder.build();
   
        
        admin.createTable(tableDescriptor);
    
        System.out.println(namespaceName+":"+tableName+"  已创建");
    
    
        admin.close();
    
    }
    
    
}
