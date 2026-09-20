package com.trade.virtual_trade.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类，解决Vue前端调用后端接口的跨域问题
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 对所有接口路径生效
        registry.addMapping("/**")
                // 允许来自Vue前端地址的请求（支持多个开发端口）
                .allowedOrigins("http://localhost:8080", "http://localhost:8082", "http://localhost:8083")
                // 允许携带Cookie（会话信息）
                .allowCredentials(true)
                // 允许所有请求方法（GET/POST/PUT/DELETE等）
                .allowedMethods("*")
                // 允许所有请求头
                .allowedHeaders("*");
    }
}