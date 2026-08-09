package com.example.goldprice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI goldPriceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Gold Price API")
                .description("API quản lý, tra cứu và tự động đồng bộ giá vàng")
                .version("1.0.0")
                .contact(new Contact().name("Nguyễn Đức Khởi")));
    }
}
