package com.trade.virtual_trade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类
 * @MapperScan 扫描Mapper接口所在的包，让MyBatis找到所有Mapper
 */
@SpringBootApplication
@MapperScan("com.trade.virtual_trade.mapper")
public class VirtualTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(VirtualTradeApplication.class, args);
    }
}