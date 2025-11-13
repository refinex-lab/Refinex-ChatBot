package cn.refinex.core.autoconfigure;

import cn.refinex.core.autoconfigure.properties.RefinexLoggingProperties;
import cn.refinex.core.autoconfigure.properties.RefinexProperties;
import cn.refinex.core.service.CryptoService;
import cn.refinex.core.util.SnowflakeIdUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Refinex 核心自动配置类
 *
 * @author Refinex
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties({RefinexProperties.class, RefinexLoggingProperties.class})
public class RefinexCoreAutoConfiguration {

    /**
     * 系统统一加解密服务
     */
    @Bean
    public CryptoService cryptoService(RefinexProperties refinexProperties) {
        return new CryptoService(refinexProperties);
    }

    /**
     * 雪花算法ID生成器
     */
    @Bean
    public SnowflakeIdUtils snowflakeIdGenerator() {
        // 工作节点ID (0-31)
        long workerId = 0;
        // 数据中心ID (0-31)
        long datacenterId = 0;
        // 容忍的最大时钟回拨毫秒数，默认2ms
        long maxBackwardMs = 2;
        return new SnowflakeIdUtils(workerId, datacenterId, maxBackwardMs);
    }
}
