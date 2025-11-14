package cn.refinex.redis.autoconfigure;

import cn.hutool.core.util.ReflectUtil;
import cn.refinex.redis.autoconfigure.properties.RefinexRedisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jspecify.annotations.NonNull;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 基于 Redis 的自动配置类，优先于 {@link RedissonAutoConfigurationV2} 执行
 * copy from <a href="https://github.com/YunaiV/yudao-cloud/blob/master/yudao-framework/yudao-spring-boot-starter-redis/src/main/java/cn/iocoder/yudao/framework/redis/config/YudaoRedisAutoConfiguration.java">...</a>
 *
 * @author 芋道源码
 * @since 1.0.0
 */
@AutoConfiguration(before = RedissonAutoConfigurationV2.class)
@EnableConfigurationProperties({RefinexRedisProperties.class, RedisProperties.class})
public class RefinexRedisAutoConfiguration {

    /**
     * 创建 RedisTemplate Bean，使用 JSON 序列化方式
     *
     * @param factory Redis 连接工厂
     * @return RedisTemplate 实例
     * @throws NullPointerException 如果 factory 为 null
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(@NonNull RedisConnectionFactory factory) {
        // 创建 RedisTemplate 对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置 RedisConnection 工厂
        template.setConnectionFactory(factory);
        // 使用 String 序列化方式，序列化 KEY 和 HASH KEY
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        // 使用 JSON 序列化方式序列化 VALUE 和 HASH VALUE
        RedisSerializer<?> serializer = buildRedisSerializer();
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        return template;
    }

    /**
     * 构建 JSON 序列化器，支持 LocalDateTime 等 Java 8 时间类型的序列化。
     *
     * @return RedisSerializer 实例
     * @throws IllegalStateException 如果无法创建 JSON 序列化器
     */
    public static RedisSerializer<@NonNull Object> buildRedisSerializer() {
        // 创建 Jackson JSON 序列化器
        RedisSerializer<@NonNull Object> json = RedisSerializer.json();

        // 配置 ObjectMapper 支持 Java 8 时间类型
        ObjectMapper objectMapper = (ObjectMapper) ReflectUtil.getFieldValue(json, "mapper");
        objectMapper.registerModules(new JavaTimeModule());
        return json;
    }

}
