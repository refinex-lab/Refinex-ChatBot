package cn.refinex.core.autoconfigure;

import cn.refinex.core.autoconfigure.properties.RefinexLoggingProperties;
import cn.refinex.core.logging.handler.JdbcRequestLogHandler;
import cn.refinex.core.logging.handler.RequestLogHandler;
import cn.refinex.core.logging.handler.Slf4jRequestLogHandler;
import cn.refinex.core.logging.interceptor.TracePropagationRequestInterceptor;
import cn.refinex.json.util.JsonUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * 日志能力自动配置
 *
 * @author Refinex
 * @since 1.0.0
 */
@AutoConfiguration(after = RefinexCoreAutoConfiguration.class)
@EnableConfigurationProperties(RefinexLoggingProperties.class)
public class RefinexLoggingAutoConfiguration {

    /**
     * RestClient Trace 传播拦截器
     */
    @Bean
    @ConditionalOnProperty(prefix = "refinex.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ClientHttpRequestInterceptor tracePropagationRequestInterceptor(RefinexLoggingProperties properties) {
        return new TracePropagationRequestInterceptor(properties);
    }

    /**
     * JDBC 持久化处理器
     */
    @Bean
    @ConditionalOnClass(NamedParameterJdbcTemplate.class)
    @ConditionalOnBean(NamedParameterJdbcTemplate.class)
    @ConditionalOnProperty(prefix = "refinex.logging.request-log", name = "persist", havingValue = "true", matchIfMissing = true)
    @Primary
    public RequestLogHandler jdbcRequestLogHandler(NamedParameterJdbcTemplate jdbcTemplate, RefinexLoggingProperties properties) {
        return new JdbcRequestLogHandler(jdbcTemplate, properties);
    }

    /**
     * 默认日志处理器
     */
    @Bean
    @ConditionalOnMissingBean(RequestLogHandler.class)
    public RequestLogHandler slf4jRequestLogHandler(@Nullable RefinexLoggingProperties properties, JsonUtils jsonUtils) {
        return new Slf4jRequestLogHandler(properties, jsonUtils);
    }
}
