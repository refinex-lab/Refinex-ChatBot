package cn.refinex.core.logging.context;

import org.jspecify.annotations.Nullable;

/**
 * HTTP 请求 Trace 上下文
 *
 * @author Refinex
 * @since 1.0.0
 */
public record TraceContext(

        /*
            Trace ID
         */
        String traceId,

        /*
            数据签名
         */
        String dataSign,

        /*
            请求开始时间，单位毫秒
         */
        long startTimeMillis,

        /*
            请求URI
         */
        @Nullable String requestUri,

        /*
            HTTP 方法
         */
        @Nullable String httpMethod,

        /*
            客户端IP
         */
        @Nullable String clientIp
) { }
