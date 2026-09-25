package com.distrimarket.inventario.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Permite que el navegador acceda a http://localhost:8081/openapi.yaml
        registry.addResourceHandler("/openapi.yaml")
                .addResourceLocations("classpath:/static/openapi.yaml", "classpath:/openapi.yaml");
    }
}