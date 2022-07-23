package com.atguigu.gmall.publisher;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall.publisher.mapper") // 2022/7/17 10:48 NOTE 要实现哪个接口
public class Gmall1118PublisherApplication {

    public static void main(String[] args) {
        SpringApplication.run(Gmall1118PublisherApplication.class, args);
    }

}
