package com.distrimarket.inventario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${info.app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DistriMarket - Microservicio de Inventario")
                        .version(appVersion)
                        .description("API REST para la gestión del microservicio inventario de DistriMarket.")
                        .contact(new Contact()
                                .name("Soporte DistriMarket")
                                .email("soporte@distrimarket.com")));
    }
}