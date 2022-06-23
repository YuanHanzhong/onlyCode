import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.List;

public class _03_Tools {

    // 获取连接
    public static Connection getConnection() {
        
        Connection connection = null;
        Configuration configuration = new Configuration();
        configuration.set("hbase.zookeeper.quorum", "hadoop102,hadoop103,hadoop104");
        
        try {
           connection = ConnectionFactory.createConnection(configuration);
    
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("获取连接成功");
    
        return connection;
    
    }
    
    
    // 删表
    public static void deleteTable(String nameSpacceName, String tableName) throws IOException {
        Admin admin = getConnection().getAdmin();
    
        TableName tableNameObj = TableName.valueOf(nameSpacceName, tableName);
        
        admin.disableTable(tableNameObj);
        admin.deleteTable(tableNameObj);
        admin.close();
    
        System.out.println(tableName+"删除成功");
    }
    
    // 建表
    public static void createTable(String nameSpacceName, String tableName, String... cfs) throws IOException {
    
        if (nameSpacceName == null || nameSpacceName.length()<1) {
            nameSpacceName = "default";
        }
    
        TableName tableNameObj = TableName.valueOf(nameSpacceName, tableName);
        TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(tableNameObj);
    
        // 设置列族, 一次只能设置1个, 所以循环设置
        for (String cf : cfs) {
            ColumnFamilyDescriptor familyDescriptor = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(cf)).build();
            tableDescriptorBuilder.setColumnFamily(familyDescriptor);
        }
        TableDescriptor tableDescriptor = tableDescriptorBuilder.build();
        Admin admin = getConnection().getAdmin();
        admin.createTable(tableDescriptor);
        admin.close();
        System.out.println("tableName = " + tableName+"创建成功");
    }
    
    // 创建 namespace
    public static void createNamespace(String nameSpacceName) throws IOException {
    
        Admin admin = getConnection().getAdmin();
        NamespaceDescriptor newNamespace = NamespaceDescriptor.create(nameSpacceName).build();
        admin.createNamespace(newNamespace);
        admin.close();
        System.out.println("  namespace: "+nameSpacceName+"创建成功");
    
    }
    
    // 删除 namespace
    public static void deleteNamespace(String nameSpacceName) throws IOException {
        Admin admin = getConnection().getAdmin();
        admin.deleteNamespace(nameSpacceName);
        System.out.println(nameSpacceName+"删除成功");
        admin.close(); // get了之后, 紧接着写close
    
    }
    
    
    // 插入数据
    public static void putCell(String nameSpacceName, String nameTableName,String rowkey, String cf, String qualifier, String value) throws IOException {
        Table table = _03_Tools.getConnection().getTable(TableName.valueOf(nameSpacceName, nameTableName));
    
        Put put = new Put(Bytes.toBytes(rowkey));
        put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(qualifier), Bytes.toBytes(value));
    
        table.put(put);
    
        Get get = new Get(Bytes.toBytes(rowkey));
        Result result = table.get(get);
    
        byte[] value1 = result.getValue(Bytes.toBytes(cf), Bytes.toBytes(qualifier));
    
        System.out.println("Bytes.toString(value1) = " + Bytes.toString(value1));
    
        List<Cell> cells = result.listCells();
        for (Cell cell : cells) {
            System.out.println("cell = " + cell);
            String s = Bytes.toString(CellUtil.cloneRow(cell))+ ":" +
                         Bytes.toString(CellUtil.cloneValue(cell));
            System.out.println("s = " + s);
        }
    
    
        table.close();
        
        
    
    }

    
    
    
}
