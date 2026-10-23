package com.distrimarket.inventario.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "distrimarket.cache")
public class DistrimarketCacheProperties {

    private boolean enabled = true;
    private String keyPrefix = "distrimarket:inventario";
    private Duration defaultTtl = Duration.ofMinutes(10);
    private int scanBatchSize = 100;
    private Map<String, Duration> domains = new HashMap<>();
}