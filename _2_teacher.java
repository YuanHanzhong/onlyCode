
public class TableProcessFunction extends BroadcastProcessFunction<JSONObject, String, JSONObject> {
    
    private DruidDataSource druidDataSource;
    
    private MapStateDescriptor<String, TableProcess> mapStateDescriptor;
    
    public TableProcessFunction(MapStateDescriptor<String, TableProcess> mapStateDescriptor) {
        this.mapStateDescriptor = mapStateDescriptor;
    }
    
    @Override
    public void open(Configuration parameters) throws Exception {
        //获取连接池对象
        druidDataSource = DruidDSUtil.createDataSource();
    }
    
    //处理主流中的数据
    @Override
    public void processElement(JSONObject jsonObj, ReadOnlyContext ctx, Collector<JSONObject> out) throws Exception {
        //获取广播状态
        ReadOnlyBroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
        
        //根据处理业务流数据的表名，到广播中获取配置对象。
        String tableName = jsonObj.getString("table");
        
        
        TableProcess tableProcess = broadcastState.get(tableName);
        if(tableProcess != null){
            //获取当前操作影响的业务记录
            JSONObject dataJsonObj = jsonObj.getJSONObject("data");
            
            //在向下游传递数据前，将不需要传递的属性过滤掉
            String sinkColumns = tableProcess.getSinkColumns();
            filterColumn(dataJsonObj,sinkColumns);
            
            //在向下游传递数据前，应该补充维度发送的目的地 sink_table
            String sinkTable = tableProcess.getSinkTable();
            dataJsonObj.put("sink_table",sinkTable);
            
            String type = jsonObj.getString("type");
            dataJsonObj.put("type",type);
            
            //如果获取到了，说明当前处理的业务流数据是维度
            out.collect(dataJsonObj);
        }else{
            //从广播状态(配置表中)没有获取到配置信息，说明不是维度
            System.out.println("$$$不是维度，过滤掉" + jsonObj);
        }
    }
    
    /*{"before":null,"after":{"source_table":"base_province","sink_table":"dim_base_province",
    "sink_columns":"id,name,region_id,area_code,iso_code,iso_3166_2","sink_pk":null,"sink_extend":null},
    "source":{"version":"1.5.4.Final","connector":"mysql","name":"mysql_binlog_source","ts_ms":1652837005681,
    "snapshot":"false","db":"gmall2022_config","sequence":null,"table":"table_process","server_id":0,"gtid":null,
    "file":"","pos":0,"row":0,"thread":null,"query":null},"op":"r","ts_ms":1652837005681,"transaction":null}*/
    //处理广播流中数据
    @Override
    public void processBroadcastElement(String jsonStr, Context ctx, Collector<JSONObject> out) throws Exception {
        //为了处理方便，可以将从配置表中读取的一条数据由jsonstr转换为json对象
        JSONObject jsonObj = JSON.parseObject(jsonStr);
        //获取配置表中的一条记录 ，将读取这条配置信息直接封装为TableProcess对象
        TableProcess tableProcess = jsonObj.getObject("after", TableProcess.class);
        
        //获取业务数据库表名
        String sourceTable = tableProcess.getSourceTable();
        //获取数仓中维度表名
        String sinkTable = tableProcess.getSinkTable();
        //获取数仓中维度表中字段
        String sinkColumns = tableProcess.getSinkColumns();
        //获取数仓中维度表中主键字段
        String sinkPk = tableProcess.getSinkPk();
        //获取建表扩展语句
        String sinkExtend = tableProcess.getSinkExtend();
        
        //提前创建维度表
        checkTable(sinkTable, sinkColumns, sinkPk, sinkExtend);
        
        //获取广播状态
        BroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
        //将配置表中的配置放到广播状态中保存起来
        broadcastState.put(sourceTable,tableProcess);
    }
    
    //创建维度表
    private void checkTable(String tableName, String columnStr, String pk, String ext) {
        //对空值进行处理
        if (ext == null) {
            ext = "";
        }
        if (pk == null) {
            pk = "id";
        }
        //拼接建表语句
        StringBuilder createSql = new StringBuilder("create table if not exists " + GmallConfig.PHOENIX_SCHEMA + "." + tableName + "(");
        
        String[] columnArr = columnStr.split(",");
        for (int i = 0; i < columnArr.length; i++) {
            String column = columnArr[i];
            if (column.equals(pk)) {
                createSql.append(column + " varchar primary key");
            } else {
                createSql.append(column + " varchar");
            }
            //除了最后一个字段后面不加逗号，其它的字段都需要在后面加一个逗号
            if (i < columnArr.length - 1) {
                createSql.append(",");
            }
        }
        createSql.append(") " + ext);
        System.out.println("在phoenix中执行的建表语句: " + createSql);
        
        try {
            //从连接池中获取连接对象
            Connection conn = druidDataSource.getConnection();
            PhoenixUtil.executeSQL(createSql.toString(),conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //过滤字段  dataJsonObj: {"tm_name":"xzls11","logo_url":"dfas","id":12}
    //sinkColumns:  id,tm_name
    private void filterColumn(JSONObject dataJsonObj, String sinkColumns) {
        String[] fieldArr = sinkColumns.split(",");
        List<String> fieldList = Arrays.asList(fieldArr);
        //获取json对象中所有的元素
        Set<Map.Entry<String, Object>> entrySet = dataJsonObj.entrySet();
        entrySet.removeIf(entry->!fieldList.contains(entry.getKey()));
    }
}
