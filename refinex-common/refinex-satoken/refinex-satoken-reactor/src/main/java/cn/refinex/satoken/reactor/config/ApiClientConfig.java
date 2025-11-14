package cn.refinex.satoken.reactor.config;

import cn.hutool.core.util.IdUtil;
import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.autoconfigure.properties.RefinexLoggingProperties;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.exception.SystemException;
import cn.refinex.core.logging.constants.LoggingConstants;
import cn.refinex.core.logging.context.TraceContext;
import cn.refinex.core.logging.context.TraceContextHolder;
import cn.refinex.core.util.StringUtils;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * OpenFeign 客户端配置与启用
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class ApiClientConfig {

    /**
     * 将 TraceId / DataSign 透传到 Feign 请求
     *
     * @param properties 日志配置属性
     * @return Feign 请求拦截器实例
     */
    @Bean
    public RequestInterceptor feignTracePropagationInterceptor(RefinexLoggingProperties properties) {
        return template -> {
            TraceContext context = TraceContextHolder.get();
            String dataSign = context != null ? context.dataSign() : MDC.get(LoggingConstants.MDC_DATA_SIGN);
            String traceId = context != null ? context.traceId() : MDC.get(LoggingConstants.MDC_TRACE_ID);
            if (StringUtils.isBlank(dataSign)) {
                dataSign = IdUtil.fastSimpleUUID();
            }
            if (StringUtils.isBlank(traceId)) {
                traceId = IdUtil.fastSimpleUUID();
            }
            if (!template.headers().containsKey(properties.getDataSignHeader())) {
                template.header(properties.getDataSignHeader(), dataSign);
            }
            if (!template.headers().containsKey(properties.getTraceIdHeader())) {
                template.header(properties.getTraceIdHeader(), traceId);
            }
        };
    }

    /**
     * 统一错误解码，将 HTTP 状态码映射为业务异常。
     *
     * @return Feign 错误解码器实例
     */
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new LoggingErrorDecoder();
    }

    /**
     * 带日志的 ErrorDecoder 实现，集中处理状态码映射与日志输出。
     */
    private static final class LoggingErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, feign.Response response) {
            int status = response.status();
            String body = readBody(response);

            ApiStatus apiStatus = ApiStatus.valueOf(status);
            switch (status) {
                case 429:
                    log.warn("Feign 429 Too Many Requests: {} -> {}", methodKey, body);
                    return new BusinessException(ApiStatus.TOO_MANY_REQUESTS);
                case 401, 403:
                    log.warn("Feign {} Unauthorized/Forbidden: {} -> {}", status, methodKey, body);
                    return new BusinessException(ApiStatus.UNAUTHORIZED);
                case 400:
                    log.warn("Feign 400 Bad Request: {} -> {}", methodKey, body);
                    return new BusinessException(ApiStatus.BAD_REQUEST);
                case 404:
                    log.warn("Feign 404 Not Found: {} -> {}", methodKey, body);
                    return new BusinessException(ApiStatus.NOT_FOUND);
                case 503:
                    log.warn("Feign 503 Service Unavailable: {} -> {}", methodKey, body);
                    return new SystemException(ApiStatus.SERVICE_UNAVAILABLE);
                default:
                    // fall through
            }

            if (apiStatus.is4xxClientError()) {
                log.warn("Feign {} Client Error: {} -> {}", status, methodKey, body);
                return new BusinessException(apiStatus);
            }
            if (apiStatus.is5xxServerError()) {
                log.warn("Feign {} Server Error: {} -> {}", status, methodKey, body);
                return new SystemException(apiStatus);
            }
            // 其他情况交由默认处理
            return feign.FeignException.errorStatus(methodKey, response);
        }

        // 尝试读取响应体内容，仅用于日志辅助，不影响主逻辑
        private static String readBody(feign.Response response) {
            if (response.body() == null) {
                return null;
            }
            try {
                return new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException ignored) {
                return null;
            }
        }
    }
}
