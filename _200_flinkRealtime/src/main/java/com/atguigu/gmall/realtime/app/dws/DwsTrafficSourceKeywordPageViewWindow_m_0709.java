package com.atguigu.gmall.realtime.app.dws;

import com.atguigu.gmall.realtime.app.func.KeywordUDTF;
import com.atguigu.gmall.realtime.bean.KeywordBean;
import com.atguigu.gmall.realtime.common.GmallConstant;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/*
 * Desc: 流量域来源关键词粒度页面浏览各窗口轻度聚合
 * 需要启动的进程
 *      zk、kafka、flume、clickhouse、DwdTrafficBaseLogSplit、DwsTrafficSourceKeywordPageViewWindow
 * 执行流程
 *      运行模拟生成日志数据的jar包
 *      会将生成的日志数据落盘到hadoop102以及hadoop103的指定目录文件中
 *      flume会从指定的文件中读取日志数据，发送到kafka的topic_log
 *      DwdTrafficBaseLogSplit会从topic_log中读取日志数据进行分流
 *          错误日志----dwd_traffic_err_log
 *          启动日志----dwd_traffic_start_log
 *          曝光日志----dwd_traffic_display_log
 *          动作日志----dwd_traffic_action_log
 *          页面日志----dwd_traffic_page_log
 *      DwsTrafficSourceKeywordPageViewWindow从页面日志dwd_traffic_page_log读取数据创建动态表（指定watermark以及提取事件时间字段）
 *          过滤出搜索行为
 *          对搜索内容进行分词并和原表数据进行连接
 *          分组、开窗、聚合计算
 *          将动态表转换为流
 *          将流中的数据写到CK的表中
 */
public class DwsTrafficSourceKeywordPageViewWindow_m_0709 {
    public static void main(String[] args) throws Exception {
        //TODO 1. 基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //1.3 指定表执行环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        //1.4 注册自定义函数, 名字随意
        tableEnv.createTemporaryFunction("ik_analyze", KeywordUDTF.class);

        //TODO 2.检查点相关的设置(略)

        //TODO 3.从kafka的dwd_traffic_page_log中读取数据创建动态表
        String topic = "dwd_traffic_page_log";
        String groupId = "dws_traffic_sourcekeyword_group";

        tableEnv.executeSql("CREATE TABLE page_log (\n" +
            "  `common` map<string,string>,\n" +
            "  `page` map<string,string>,\n" +
            "  `ts` bigint,\n" +
            "  rowtime as TO_TIMESTAMP(FROM_UNIXTIME(ts/1000)),\n" + // 2022/7/10 9:31 NOTE 值得走走
            "  WATERMARK FOR rowtime AS rowtime - INTERVAL '3' SECOND\n" +
            ")" + MyKafkaUtil.getKafkaDDL(topic, groupId));
    

        //TODO 4.从页面日志中将搜索行为过滤出来
        Table filterTable = tableEnv.sqlQuery("select\n" +
            " page['item'] fullword,\n" +
            " rowtime\n" +
            "from \n" +
            " page_log\n" +
            "where \n" +
            " page['page_id']='good_list' and page['last_page_id']='search' and page['item_type']='keyword'\n" +
            " and page['item'] is not null");

        tableEnv.createTemporaryView("filter_table", filterTable);
        
        // 2022/7/10 9:32 NOTE ASK 想要立刻看下上表数据, 就创建按临时视图
        //tableEnv.executeSql("select * from filter_table").print(); // 2022/7/10 15:25 NOTE GTO table的只能1次, 有了这个, 下面就不执行了

        //TODO 5.使用自定义函数，对搜索内容进行分词  并和原有字段进行连接
        Table keywordTable = tableEnv.sqlQuery("SELECT keyword,rowtime FROM filter_table,LATERAL TABLE(ik_analyze(fullword)) t(keyword)");
        tableEnv.createTemporaryView("keyword_table", keywordTable);


        //TODO 6.分组、开窗、聚合计算
        Table reduceTable = tableEnv.sqlQuery("select\n" +
            " DATE_FORMAT(TUMBLE_START(rowtime, INTERVAL '10' SECOND), 'yyyy-MM-dd HH:mm:ss') stt,\n" +
            " DATE_FORMAT(TUMBLE_END(rowtime, INTERVAL '10' SECOND), 'yyyy-MM-dd HH:mm:ss') edt,\n" +
            " '" + GmallConstant.KEYWORD_SEARCH + "' as source,\n" +
            " keyword,\n" +
            " count(*) keyword_count,\n" +
            " UNIX_TIMESTAMP()*1000 ts\n" +
            "from\n" +
            " keyword_table\n" +
            "group by \n" +
            " TUMBLE(rowtime, INTERVAL '10' SECOND),keyword");

        //tableEnv.executeSql("select * from " + reduceTable).print();

        //TODO 7.将聚合的结果转换为流
        DataStream<KeywordBean> keywordDS = tableEnv.toAppendStream(reduceTable, KeywordBean.class);

        keywordDS.print(">>>>");

        //TODO 8.将流中的数据写到ClickHouse中
        keywordDS.addSink(
            MyClickHouseUtil.getJdbcSink("insert into dws_traffic_source_keyword_page_view_window values(?,?,?,?,?,?)")
        );

        env.execute();
    }
}
