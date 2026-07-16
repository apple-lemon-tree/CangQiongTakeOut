package com.abc.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/*
Spring Cache + Redis集成配置
作用：配置Spring的缓存管理器(CacheManager)，从而可以使用@Cacheable,@CacheEvict等注解来操作Redis缓存
 */
@Configuration
public class RedisConfig{
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory){
        GenericJacksonJsonRedisSerializer serializer =
                GenericJacksonJsonRedisSerializer.builder()
                        .enableSpringCacheNullValueSupport()
                        .build();
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
        System.out.println("自定义 RedisCacheManager 已加载");
        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }
}
