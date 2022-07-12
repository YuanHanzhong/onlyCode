package com.atguigu.gmall.realtime.app.func;

import com.atguigu.gmall.realtime.util.KeywordUtil;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;

/*
 * Author: Felix
 * Date: 2022/5/28
 * Desc: 自定义UDTF函数  完成分词功能
 */
@FunctionHint(output = @DataTypeHint("ROW<word STRING>"))
public class KeywordUDTF_m_0709 extends TableFunction<Row> {
    // 2022/7/9 15:28 NOTE 自定义函数, 不确定参数个数, 类型, 所以不能在父类中定义为接口
    // 2022/7/9 15:29 NOTE 方法名固定, 必须为eval, 可以重载
    public void eval(String text) {
        for (String keyword : KeywordUtil.analyze(text)) {
            collect(Row.of(keyword));
            System.out.println();
        }
    }
}
