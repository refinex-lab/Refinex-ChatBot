package cn.refinex.core.logging.interceptor;

import cn.hutool.core.util.IdUtil;
import cn.refinex.core.autoconfigure.properties.RefinexLoggingProperties;
import cn.refinex.core.logging.constants.LoggingConstants;
import cn.refinex.core.logging.context.TraceContext;
import cn.refinex.core.logging.context.TraceContextHolder;
import cn.refinex.core.util.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * 将 TraceId / DataSign 透传到 RestClient
 *
 * @author Refinex
 * @since 1.0.0
 */
public record TracePropagationRequestInterceptor(RefinexLoggingProperties properties) implements ClientHttpRequestInterceptor, Ordered {

    /**
     * 拦截 HTTP 请求，添加 TraceId 和 DataSign 到请求头中
     *
     * @param request   HTTP 请求对象
     * @param body      请求体字节数组
     * @param execution 请求执行器
     * @return 响应对象
     * @throws IOException 输入输出异常
     */
    @NullMarked
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 从 TraceContextHolder 获取 TraceContext，如果没有则使用 MDC 作为回退
        TraceContext context = TraceContextHolder.get();
        String dataSign = context != null ? context.dataSign() : fallbackMdc(LoggingConstants.MDC_DATA_SIGN);
        String traceId = context != null ? context.traceId() : fallbackMdc(LoggingConstants.MDC_TRACE_ID);

        if (StringUtils.isBlank(dataSign)) {
            dataSign = IdUtil.fastSimpleUUID();
        }
        if (StringUtils.isBlank(traceId)) {
            traceId = IdUtil.fastSimpleUUID();
        }

        // 检查请求头中是否已包含 DataSign 和 TraceId，如果没有则添加
        if (!request.getHeaders().toSingleValueMap().containsKey(properties.getDataSignHeader())) {
            request.getHeaders().set(properties.getDataSignHeader(), dataSign);
        }
        if (!request.getHeaders().toSingleValueMap().containsKey(properties.getTraceIdHeader())) {
            request.getHeaders().set(properties.getTraceIdHeader(), traceId);
        }

        // 继续执行请求
        return execution.execute(request, body);
    }

    /**
     * 获取拦截器的执行顺序，优先级最高，以确保 TraceId 和 DataSign 在其他拦截器之前被添加到请求头中
     *
     * @return 拦截器的执行顺序，优先级最高
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 从 MDC 中获取值作为回退
     *
     * @param key MDC 键
     * @return MDC 值或 null
     */
    private String fallbackMdc(String key) {
        String value = MDC.get(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return null;
    }
}
