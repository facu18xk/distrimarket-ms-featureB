package com.distrimarket.inventario.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
public class RedisCacheConfig {

    @Value("${cache.categoria.ttl-seconds:3600}")
    private long categoriaTtl;

    @Value("${cache.marca.ttl-seconds:3600}")
    private long marcaTtl;

    @Value("${cache.producto.ttl-seconds:900}")
    private long productoTtl;

    @Value("${cache.empleado.ttl-seconds:1800}")
    private long empleadoTtl;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        // Aplicamos los TTLs configurados en el YAML dinámicamente
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("categorias", baseConfig.entryTtl(Duration.ofSeconds(categoriaTtl)));
        cacheConfigurations.put("marcas", baseConfig.entryTtl(Duration.ofSeconds(marcaTtl)));
        cacheConfigurations.put("productos", baseConfig.entryTtl(Duration.ofSeconds(productoTtl)));
        cacheConfigurations.put("empleados", baseConfig.entryTtl(Duration.ofSeconds(empleadoTtl)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}