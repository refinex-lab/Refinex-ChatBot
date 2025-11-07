package cn.refinex.redis.autoconfigure;

import cn.hutool.core.util.StrUtil;
import cn.refinex.redis.cache.TimeoutRedisCacheManager;
import cn.refinex.redis.autoconfigure.properties.RefinexRedisProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 基于 Redis 的缓存自动配置类。
 * reference from <a href="https://github.com/YunaiV/yudao-cloud/blob/master/yudao-framework/yudao-spring-boot-starter-redis/src/main/java/cn/iocoder/yudao/framework/redis/config/YudaoCacheAutoConfiguration.java">...</a>
 *
 * <p>
 * 配置 Redis 缓存，包括自定义过期时间的 RedisCacheManager 和 RedisCacheConfiguration。
 * </p>
 *
 * @author 芋道源码
 * @author Refinex
 * @since 1.0.0
 */
@EnableCaching
@AutoConfiguration
@EnableConfigurationProperties(value = {CacheProperties.class})
public class RefinexCacheAutoConfiguration {

    /**
     * 配置 RedisCacheConfiguration。
     * <p>
     * 1. 设置缓存键前缀为单冒号（:），避免 Redis Desktop Manager 显示多余空格。
     * 2. 使用 JSON 序列化方式存储缓存值。
     * 3. 应用 CacheProperties.Redis 的配置（如 TTL、是否缓存空值、是否使用键前缀）。
     * </p>
     *
     * @param cacheProperties Spring Boot 缓存属性配置
     * @return RedisCacheConfiguration 实例
     */
    @Bean
    @Primary
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

        // 设置缓存键前缀，使用单冒号（:）分隔
        config = config.computePrefixWith(cacheName -> {
            String keyPrefix = cacheProperties.getRedis().getKeyPrefix();
            if (StringUtils.hasText(keyPrefix)) {
                // 确保前缀以冒号结尾
                String prefix = keyPrefix.endsWith(StrUtil.COLON) ? keyPrefix : keyPrefix + StrUtil.COLON;
                return prefix + cacheName + StrUtil.COLON;
            }
            return cacheName + StrUtil.COLON;
        });

        // 设置 JSON 序列化方式
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(RefinexRedisAutoConfiguration.buildRedisSerializer()));

        // 应用 CacheProperties.Redis 配置
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }

        return config;
    }

    /**
     * 配置 RedisCacheManager，支持自定义过期时间。
     *
     * @param redisTemplate           Redis 模板
     * @param redisCacheConfiguration Redis 缓存配置
     * @param refinexRedisProperties  自定义缓存属性配置
     * @return RedisCacheManager 实例
     * @throws IllegalArgumentException 如果 redisScanBatchSize 小于或等于 0
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisTemplate<String, Object> redisTemplate, RedisCacheConfiguration redisCacheConfiguration, RefinexRedisProperties refinexRedisProperties) {
        int batchSize = refinexRedisProperties.getRedisScanBatchSize();
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Redis 扫描一次返回数量必须大于 0");
        }

        // 创建 RedisCacheWriter, 设置批量扫描大小
        RedisConnectionFactory connectionFactory = Objects.requireNonNull(redisTemplate.getConnectionFactory(), "RedisConnectionFactory must not be null");
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory, BatchStrategies.scan(batchSize));

        // 创建 TimeoutRedisCacheManager
        return new TimeoutRedisCacheManager(cacheWriter, redisCacheConfiguration);
    }
}
