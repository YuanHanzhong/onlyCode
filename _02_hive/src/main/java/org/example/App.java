package org.example;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;

/**
 * Hello world!
 *
 */
public class App extends GenericUDF
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
    
    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        return null;
    }
    
    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        return null;
    }
    
    @Override
    public String getDisplayString(String[] children) {
        return null;
    }
}
