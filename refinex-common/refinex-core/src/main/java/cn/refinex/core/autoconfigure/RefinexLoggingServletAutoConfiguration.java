package cn.refinex.core.autoconfigure;

import cn.refinex.core.autoconfigure.properties.RefinexLoggingProperties;
import cn.refinex.core.logging.aspect.RequestLogAspect;
import cn.refinex.core.logging.filter.TraceLoggingFilter;
import cn.refinex.core.logging.handler.RequestLogHandler;
import cn.refinex.json.util.JsonUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * Servlet 环境专用日志自动配置
 *
 * 将依赖 Servlet API 的 Bean 放在独立的 AutoConfiguration，
 * 通过字符串形式的 @ConditionalOnClass 避免在 WebFlux 环境下类加载失败。
 */
@AutoConfiguration(after = RefinexCoreAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = {
        "jakarta.servlet.Filter",
        "org.springframework.web.filter.OncePerRequestFilter",
        "org.springframework.boot.web.servlet.FilterRegistrationBean"
})
public class RefinexLoggingServletAutoConfiguration {

    /**
     * Trace + DataSign 过滤器
     */
    @Bean
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
    public FilterRegistrationBean<TraceLoggingFilter> traceLoggingFilterRegistration(TraceLoggingFilter filter) {
        FilterRegistrationBean<TraceLoggingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        return registration;
    }

    /**
     * 请求日志切面（Servlet 环境）
     */
    @Bean
    @ConditionalOnProperty(prefix = "refinex.logging.request-log", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RequestLogAspect requestLogAspect(RequestLogHandler requestLogHandler, RefinexLoggingProperties properties, Environment environment, JsonUtils jsonUtils) {
        return new RequestLogAspect(requestLogHandler, properties, environment, jsonUtils);
    }
}
