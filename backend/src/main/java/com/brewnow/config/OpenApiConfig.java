package com.brewnow.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI brewNowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("BrewNow 沏刻茶叶电商平台 API")
                        .description("基于 Spring Boot 的茶叶电商平台接口文档，覆盖用户、商品、订单、推荐、商家与管理员模块。")
                        .version("v1.0.0")
                        .contact(new Contact().name("BrewNow 项目组"))
                        .license(new License().name("For academic use")))
                .externalDocs(new ExternalDocumentation()
                        .description("项目后端说明")
                        .url("/api/system/docs"));
    }
}
