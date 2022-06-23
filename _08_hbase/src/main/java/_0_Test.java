import org.apache.hadoop.hbase.client.Admin;
import org.junit.Test;

import java.io.IOException;

public class _0_Test {
    public static void main(String[] args) throws IOException {
    
    
    }
    
    
    @Test
    public void createNamespace() throws IOException {
        _03_Tools.createNamespace("");
        
    }
    
    @Test
    public void createTable() throws IOException {
        _03_Tools.createTable("", "jack", "info1", "info2","info3");
        _03_Tools.createTable(null,"jack2", "info1", "info2","info3");
    }
    
    @Test
    public void deleteTable() throws IOException {
        _03_Tools.deleteTable("mynamespace1","table1");
    }
    
    @Test
    public void deleteNamespace() throws IOException {
        _03_Tools.deleteNamespace("mynamespace1");
    }
    
    @Test
    public void putCell() throws IOException {
        _03_Tools.putCell("second", "code", "1001", "info1", "num", "new inserted value");
        
        
        
    }
    
}
