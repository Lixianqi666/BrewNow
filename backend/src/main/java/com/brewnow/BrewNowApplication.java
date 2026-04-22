package com.brewnow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 沏刻茶叶电商平台启动类
 * 
 * @author brew-now
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.brewnow.mapper")
public class BrewNowApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrewNowApplication.class, args);
        System.out.println("===========================================");
        System.out.println("    沏刻茶叶电商平台启动成功!");
        System.out.println("    接口文档地址: http://localhost:8080/api");
        System.out.println("    数据库监控: http://localhost:8080/api/druid");
        System.out.println("===========================================");
    }
}
