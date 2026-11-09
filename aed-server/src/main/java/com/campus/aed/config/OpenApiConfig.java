package com.campus.aed.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 接口文档配置
 * 访问地址: http://localhost:8080/doc.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("高校AED器械管理系统 API")
                .description("基于 SpringBoot 的高校 AED 器械管理系统接口文档")
                .version("1.0.0"));
    }
}
