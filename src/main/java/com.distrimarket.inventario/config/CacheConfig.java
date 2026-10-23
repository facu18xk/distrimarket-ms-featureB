package com.distrimarket.inventario.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching(order = Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(CacheConfig.Properties.class)
public class CacheConfig implements CachingConfigurer {

    // ---------------- Dominios (secciones) de Inventario ----------------
    public static final String CATEGORIA = "categoria";
    public static final String MARCA = "marca";
    public static final String PRODUCTO = "producto";
    public static final String DEPOSITO = "deposito";
    public static final String PERSONA = "persona";
    public static final String EMPLEADO = "empleado";

    @ConfigurationProperties(prefix = "distrimarket.cache")
    public record Properties(boolean enabled, String keyPrefix, Duration defaultTtl, int scanBatchSize,
                             Map<String, Duration> domains) {

        public Properties {
            if (keyPrefix == null || keyPrefix.isBlank()) {
                throw new IllegalStateException("distrimarket.cache.key-prefix es obligatorio.");
            }
            defaultTtl = defaultTtl == null ? Duration.ofMinutes(10) : defaultTtl;
            scanBatchSize = scanBatchSize <= 0 ? 100 : scanBatchSize;
            domains = domains == null ? Map.of() : Map.copyOf(domains);
        }

        public String redisKey(String domain, Object key) {
            return keyPrefix + ":" + domain + ":" + key;
        }

        public Duration ttl(String domain) {
            return domains.getOrDefault(domain, defaultTtl);
        }

        public static String describe(Duration ttl) {
            return ttl == null || ttl.isZero() ? "persistente" : "TTL " + ttl;
        }
    }

    @Bean
    public CacheManager cacheManager(Properties properties, ObjectProvider<RedisConnectionFactory> connectionFactory) {
        if (!properties.enabled()) {
            log.warn("Caché deshabilitada (distrimarket.cache.enabled=false): todas las lecturas van a la base");
            return new LoggingCache.Manager(new NoOpCacheManager(), properties);
        }

        RedisConnectionFactory factory = connectionFactory.getIfAvailable();
        if (factory == null) {
            log.warn("No se encontró RedisConnectionFactory disponible. Usando NoOpCacheManager.");
            return new LoggingCache.Manager(new NoOpCacheManager(), properties);
        }

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .computePrefixWith(domain -> properties.keyPrefix() + ":" + domain + ":")
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer()));

        Map<String, RedisCacheConfiguration> domains = new LinkedHashMap<>();
        properties.domains().forEach((domain, ttl) -> domains.put(domain, base.entryTtl(ttl)));

        RedisCacheManager redis = RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(
                        factory, BatchStrategies.scan(properties.scanBatchSize())))
                .cacheDefaults(base.entryTtl(properties.defaultTtl()))
                .withInitialCacheConfigurations(domains)
                .disableCreateOnMissingCache()
                .build();

        redis.initializeCaches();
        properties.domains().forEach((domain, ttl) -> log.info("Caché '{}' -> claves {}<id> ({})",
                domain, properties.redisKey(domain, ""), Properties.describe(ttl)));

        // Se retorna el Manager decorado para interceptar las operaciones y emitir los logs
        return new LoggingCache.Manager(redis, properties);
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Caché no disponible en GET {}::{}; se consulta la BD ({})",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Caché no disponible en SET {}::{} ({})", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error("Caché no disponible en DELETE {}::{}; ({})", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("Caché no disponible al limpiar la sección {}; ({})", cache.getName(), exception.getMessage());
            }
        };
    }

    public static RedisSerializer<Object> valueSerializer() {
        BasicPolymorphicTypeValidator allowed = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.distrimarket.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.math.")
                .allowIfSubType("java.lang.")
                .build();

        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .activateDefaultTyping(allowed, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY)
                .build();

        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}