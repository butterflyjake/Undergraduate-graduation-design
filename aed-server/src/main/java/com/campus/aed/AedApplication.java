package com.campus.aed;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 高校AED器械管理系统 启动类
 */
@SpringBootApplication
@MapperScan("com.campus.aed.mapper")
public class AedApplication {

    public static void main(String[] args) {
        SpringApplication.run(AedApplication.class, args);
    }
}
