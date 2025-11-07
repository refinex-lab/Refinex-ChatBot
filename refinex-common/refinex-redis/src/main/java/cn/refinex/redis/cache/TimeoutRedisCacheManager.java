package cn.refinex.redis.cache;

import cn.hutool.core.util.StrUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;
import java.util.Objects;

/**
 * 支持自定义过期时间的 {@link RedisCacheManager} 实现类
 * copy from <a href="https://github.com/YunaiV/yudao-cloud/blob/master/yudao-framework/yudao-spring-boot-starter-redis/src/main/java/cn/iocoder/yudao/framework/redis/core/TimeoutRedisCacheManager.java">...</a>
 *
 * <p>
 * 1. 在 {@link org.springframework.cache.annotation.Cacheable#cacheNames()} 格式为 "key#ttl" 时，# 后面的 ttl 为过期时间。
 * 2. 单位为最后一个字母（支持的单位有：d 天，h 小时，m 分钟，s 秒），默认单位为 s 秒。
 * </p>
 *
 * @author 芋道源码
 * @since 1.0.0
 */
public class TimeoutRedisCacheManager extends RedisCacheManager {

    /**
     * 缓存名称和 TTL 的分隔符
     */
    private static final String SPLIT = "#";

    /**
     * 构造函数，初始化 RedisCacheManager。
     *
     * @param cacheWriter               Redis 缓存写入器
     * @param defaultCacheConfiguration 默认缓存配置
     * @throws NullPointerException 如果 cacheWriter 或 defaultCacheConfiguration 为 null
     */
    public TimeoutRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(
                Objects.requireNonNull(cacheWriter, "CacheWriter must not be null"),
                Objects.requireNonNull(defaultCacheConfiguration, "DefaultCacheConfiguration must not be null")
        );
    }

    /**
     * 创建 RedisCache，支持自定义过期时间。
     *
     * <p>
     * 如果缓存名称包含 # 分隔符且格式为 "key#ttl"，则解析 ttl 并设置缓存过期时间。
     * </p>
     *
     * @param name        缓存名称，可能包含 TTL（如 "key#30s"）
     * @param cacheConfig 缓存配置
     * @return RedisCache 实例
     */
    @NullMarked
    @Override
    protected RedisCache createRedisCache(String name, @Nullable RedisCacheConfiguration cacheConfig) {
        // 如果缓存名称为空，直接返回父类的缓存实例
        if (StrUtil.isEmpty(name)) {
            return super.createRedisCache(name, cacheConfig);
        }

        // 分割缓存名称，检查是否包含 TTL
        String[] names = name.split(SPLIT);
        if (names.length != 2) {
            return super.createRedisCache(name, cacheConfig);
        }

        // 解析 TTL 并更新缓存配置
        if (cacheConfig != null) {
            // 提取 TTL 部分，移除可能的冒号后缀
            int colonIndex = names[1].indexOf(':');
            String ttlStr = colonIndex >= 0 ? names[1].substring(0, colonIndex) : names[1];
            String suffix = colonIndex >= 0 ? names[1].substring(colonIndex) : "";

            // 解析 TTL 字符串为 Duration 作为 RedisCacheConfiguration 的 TTL
            Duration duration = parseDuration(ttlStr);
            cacheConfig = cacheConfig.entryTtl(duration);

            // 重构缓存名称，移除 TTL 部分
            name = names[0] + suffix;
        }

        return super.createRedisCache(name, cacheConfig);
    }

    /**
     * 解析过期时间字符串为 Duration。
     *
     * @param ttlStr 过期时间字符串（如 "30s"、"1h"）
     * @return 过期时间 Duration
     * @throws IllegalArgumentException 如果 ttlStr 格式无效或无法解析
     */
    private Duration parseDuration(String ttlStr) {
        Objects.requireNonNull(ttlStr, "TTL string must not be null");
        if (ttlStr.isBlank()) {
            throw new IllegalArgumentException("TTL string must not be empty");
        }

        String timeUnit = ttlStr.substring(ttlStr.length() - 1);
        return switch (timeUnit) {
            case "d" -> Duration.ofDays(removeDurationSuffix(ttlStr));
            case "h" -> Duration.ofHours(removeDurationSuffix(ttlStr));
            case "m" -> Duration.ofMinutes(removeDurationSuffix(ttlStr));
            case "s" -> Duration.ofSeconds(removeDurationSuffix(ttlStr));
            default -> Duration.ofSeconds(Long.parseLong(ttlStr));
        };
    }

    /**
     * 移除时间单位后缀并解析为长整型时间值。
     *
     * @param ttlStr 过期时间字符串（如 "30s"）
     * @return 时间值
     * @throws IllegalArgumentException 如果 ttlStr 格式无效或无法解析
     */
    private long removeDurationSuffix(String ttlStr) {
        String numberStr = ttlStr.substring(0, ttlStr.length() - 1);
        if (numberStr.isBlank()) {
            throw new IllegalArgumentException("Invalid TTL format: " + ttlStr);
        }

        try {
            return Long.parseLong(numberStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid TTL number format: " + numberStr, e);
        }
    }
}
