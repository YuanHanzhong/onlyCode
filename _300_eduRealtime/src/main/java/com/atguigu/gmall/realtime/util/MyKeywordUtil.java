package com.atguigu.gmall.realtime.util;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Felix
 * Date: 2022/7/9
 * Desc: 使用IK分词器进行分词的工具类
 */
public class MyKeywordUtil {
    public static List<String> analyze(String text){
        List<String> keywordList = new ArrayList<>();
        StringReader reader = new StringReader(text);
        IKSegmenter ikSegmenter = new IKSegmenter(reader,true);
        try {
            Lexeme lexeme = null;
            while((lexeme = ikSegmenter.next())!=null){
                String keyword = lexeme.getLexemeText();
                keywordList.add(keyword);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keywordList;
    }

    public static void main(String[] args) {
        List<String> kwList = analyze("Apple iPhoneXSMax (A2104) 256GB 深空灰色 移动联通电信4G手机 双卡双待");
        System.out.println(kwList);
    }
}
