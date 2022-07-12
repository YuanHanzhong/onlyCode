package com.atguigu.gmall.realtime.util;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Felix
 * Date: 2022/5/28
 * Desc: 分词工具类
 */
public class KeywordUtil_m_0709 {
    // 2022/7/9 15:13 NOTE TODO 分词器
    public static List<String> analyze(String text){
        List resList = new ArrayList();
        StringReader reader = new StringReader(text);
        IKSegmenter ikSegmenter = new IKSegmenter(reader,true);
        Lexeme lexeme = null;
        try {
            while( (lexeme = ikSegmenter.next()) != null){
                String keyword = lexeme.getLexemeText();
                resList.add(keyword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resList;
    }

    public static void main(String[] args) {
        System.out.println(analyze("Apple iPhoneXSMax (A2104) 256GB 深空灰色 移动联通电信4G手机 双卡双待"));
    }
}
