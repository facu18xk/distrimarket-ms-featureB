package com.distrimarket.inventario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${pagination.default-page:0}")
    private int defaultPage;

    @Value("${pagination.default-page-size:10}")
    private int defaultPageSize;

    @Value("${pagination.max-page-size:100}")
    private int maxPageSize;

    @Value("${pagination.one-indexed-parameters:false}")
    private boolean oneIndexedParameters;

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> {
            resolver.setMaxPageSize(maxPageSize);
            resolver.setFallbackPageable(PageRequest.of(defaultPage, defaultPageSize));
            resolver.setOneIndexedParameters(oneIndexedParameters);
        };
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Permite que el navegador acceda a http://localhost:8081/openapi.yaml
        registry.addResourceHandler("/openapi.yaml")
                .addResourceLocations("classpath:/static/openapi.yaml", "classpath:/openapi.yaml");
        // Expone el archivo que viene dentro del jar distrimarket-common
        /*registry.addResourceHandler("/v3/api-docs/yaml")
                .addResourceLocations("classpath:/static/openapi.yaml");*/
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todos los endpoints de la API
                .allowedOrigins("*") // Permite cualquier origen (Swagger UI, etc.)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*");
    }
}