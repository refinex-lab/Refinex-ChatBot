package cn.refinex.redis.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Refinex Redis 配置类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "refinex.redis")
public class RefinexRedisProperties {

    /**
     * redis scan 一次返回数量, 默认 30
     */
    private Integer redisScanBatchSize = 30;
}
