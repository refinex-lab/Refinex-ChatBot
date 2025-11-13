package cn.refinex.core.autoconfigure;

import cn.refinex.core.autoconfigure.properties.RefinexLoggingProperties;
import cn.refinex.core.logging.aspect.RequestLogAspect;
import cn.refinex.core.logging.filter.TraceLoggingFilter;
import cn.refinex.core.logging.handler.JdbcRequestLogHandler;
import cn.refinex.core.logging.handler.RequestLogHandler;
import cn.refinex.core.logging.handler.Slf4jRequestLogHandler;
import cn.refinex.core.logging.interceptor.TracePropagationRequestInterceptor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

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
     * Trace + DataSign 过滤器
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "refinex.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TraceLoggingFilter traceLoggingFilter(RefinexLoggingProperties properties) {
        return new TraceLoggingFilter(properties);
    }

    /**
     * 注册过滤器顺序
     */
    @NullMarked
    @Bean
    @ConditionalOnBean(TraceLoggingFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public FilterRegistrationBean<TraceLoggingFilter> traceLoggingFilterRegistration(TraceLoggingFilter filter) {
        FilterRegistrationBean<TraceLoggingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        return registration;
    }

    /**
     * RestClient Trace 传播拦截器
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "refinex.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ClientHttpRequestInterceptor tracePropagationRequestInterceptor(RefinexLoggingProperties properties) {
        return new TracePropagationRequestInterceptor(properties);
    }

    /**
     * 请求日志切面
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(OncePerRequestFilter.class)
    @ConditionalOnProperty(prefix = "refinex.logging.request-log", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RequestLogAspect requestLogAspect(RequestLogHandler requestLogHandler, RefinexLoggingProperties properties, Environment environment) {
        return new RequestLogAspect(requestLogHandler, properties, environment);
    }

    /**
     * JDBC 持久化处理器
     */
    @Bean
    @ConditionalOnClass(NamedParameterJdbcTemplate.class)
    @ConditionalOnBean(NamedParameterJdbcTemplate.class)
    @ConditionalOnProperty(prefix = "refinex.logging.request-log", name = "persist", havingValue = "true", matchIfMissing = true)
    public RequestLogHandler jdbcRequestLogHandler(NamedParameterJdbcTemplate jdbcTemplate, RefinexLoggingProperties properties) {
        return new JdbcRequestLogHandler(jdbcTemplate, properties);
    }

    /**
     * 默认日志处理器
     */
    @Bean
    @ConditionalOnMissingBean(RequestLogHandler.class)
    public RequestLogHandler slf4jRequestLogHandler(@Nullable RefinexLoggingProperties properties) {
        return new Slf4jRequestLogHandler(properties);
    }
}
