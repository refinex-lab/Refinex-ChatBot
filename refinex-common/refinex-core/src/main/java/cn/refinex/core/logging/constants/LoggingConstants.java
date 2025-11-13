package cn.refinex.core.logging.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 日志上下文常量
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoggingConstants {

    /**
     * 日志上下文中的Trace ID键名
     */
    public static final String MDC_TRACE_ID = "traceId";

    /**
     * 日志上下文中的数据签名键名
     */
    public static final String MDC_DATA_SIGN = "dataSign";

    /**
     * 日志上下文中的Trace上下文键名
     */
    public static final String ATTRIBUTE_TRACE_CONTEXT = LoggingConstants.class.getName() + ".TRACE_CONTEXT";

    /**
     * 日志上下文中的数据签名键名
     */
    public static final String ATTRIBUTE_DATA_SIGN = LoggingConstants.class.getName() + ".DATA_SIGN";

    /**
     * 日志上下文中的Trace ID键名
     */
    public static final String ATTRIBUTE_TRACE_ID = LoggingConstants.class.getName() + ".TRACE_ID";

    /**
     * 日志上下文中的请求开始时间键名
     */
    public static final String ATTRIBUTE_REQUEST_START_TIME = LoggingConstants.class.getName() + ".REQUEST_START_TIME";

    /**
     * 日志上下文中的请求包装器键名
     */
    public static final String ATTRIBUTE_REQUEST_WRAPPER = LoggingConstants.class.getName() + ".REQUEST_WRAPPER";

    /**
     * 日志上下文中的响应包装器键名
     */
    public static final String ATTRIBUTE_RESPONSE_WRAPPER = LoggingConstants.class.getName() + ".RESPONSE_WRAPPER";
}
