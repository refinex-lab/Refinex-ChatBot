package cn.refinex.core.logging.filter;

import cn.hutool.core.util.IdUtil;
import cn.refinex.core.autoconfigure.properties.RefinexLoggingProperties;
import cn.refinex.core.logging.constants.LoggingConstants;
import cn.refinex.core.logging.context.TraceContext;
import cn.refinex.core.logging.context.TraceContextHolder;
import cn.refinex.core.util.ServletUtils;
import cn.refinex.core.util.StringUtils;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 统一 Trace & DataSign 过滤器，完成:
 * <ul>
 *     <li>DataSign 补偿 & TraceId 生成</li>
 *     <li>MDC 填充 (traceId/dataSign)</li>
 *     <li>请求生命周期日志</li>
 *     <li>请求/响应体缓存，供 @RequestLog 使用</li>
 * </ul>
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
public class TraceLoggingFilter extends OncePerRequestFilter {

    private final RefinexLoggingProperties properties;

    /**
     * 构造函数，初始化日志配置属性
     *
     * @param properties 日志配置属性
     */
    public TraceLoggingFilter(RefinexLoggingProperties properties) {
        this.properties = properties;
    }

    /**
     * 是否不应该过滤异步调度，默认返回 false，表示异步调度也应该被过滤器处理。
     *
     * @return 是否不应该过滤异步调度
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    /**
     * 是否不应该过滤错误调度，默认返回 true，表示错误调度不应该被过滤器处理。
     *
     * @return 是否不应该过滤错误调度
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    /**
     * 是否不应该过滤请求，如果请求的调度类型不是 DispatcherType.REQUEST，则不应该过滤请求。
     *
     * @param request 当前 HTTP 请求对象
     * @return 是否不应该过滤请求
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return request.getDispatcherType() != DispatcherType.REQUEST;
    }

    /**
     * 过滤器核心逻辑，处理请求和响应，记录日志，并将 TraceId 和 DataSign 添加到请求和响应头中。
     *
     * @param request     当前 HTTP 请求对象
     * @param response    当前 HTTP 响应对象
     * @param filterChain 过滤器链对象
     * @throws ServletException 如果发生 Servlet 异常
     * @throws IOException      如果发生 IO 异常
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 如果日志功能未启用，直接继续过滤链
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 准备请求和响应包装器，设置缓存限制，并从请求头中获取 DataSign 和 TraceId，如果不存在则生成新的值
        int cacheLimit = Math.max(properties.getRequestLog().getBodyMaxLength(), 4096);
        String incomingDataSign = request.getHeader(properties.getDataSignHeader());
        String incomingTraceId = request.getHeader(properties.getTraceIdHeader());
        String dataSign = ensureIdentifier(incomingDataSign);
        String traceId = ensureIdentifier(incomingTraceId);

        // 创建 TraceAwareRequestWrapper 和 ContentCachingResponseWrapper，并将 DataSign 和 TraceId 添加到请求和响应头中
        TraceAwareRequestWrapper requestWrapper = new TraceAwareRequestWrapper(request, cacheLimit);
        requestWrapper.putHeader(properties.getDataSignHeader(), dataSign);
        requestWrapper.putHeader(properties.getTraceIdHeader(), traceId);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        responseWrapper.setHeader(properties.getDataSignHeader(), dataSign);
        responseWrapper.setHeader(properties.getTraceIdHeader(), traceId);

        // 记录请求开始时间，并将相关属性（DataSign、TraceId、请求和响应包装器）存储在请求属性中，以便后续使用
        long startTime = System.currentTimeMillis();
        requestWrapper.setAttribute(LoggingConstants.ATTRIBUTE_REQUEST_START_TIME, startTime);
        requestWrapper.setAttribute(LoggingConstants.ATTRIBUTE_DATA_SIGN, dataSign);
        requestWrapper.setAttribute(LoggingConstants.ATTRIBUTE_TRACE_ID, traceId);
        requestWrapper.setAttribute(LoggingConstants.ATTRIBUTE_REQUEST_WRAPPER, requestWrapper);
        requestWrapper.setAttribute(LoggingConstants.ATTRIBUTE_RESPONSE_WRAPPER, responseWrapper);

        // 从请求包装器中获取请求 URI、HTTP 方法和客户端 IP 地址
        String requestUri = requestWrapper.getRequestURI();
        String httpMethod = requestWrapper.getMethod();
        String clientIp = ServletUtils.getClientIp(requestWrapper);

        // 创建 TraceContext 对象，并将其存储在请求属性和 TraceContextHolder 中，同时将 TraceId 和 DataSign 添加到 MDC 中，以便在日志中使用
        TraceContext traceContext = new TraceContext(traceId, dataSign, startTime, requestUri, httpMethod, clientIp);
        requestWrapper.setAttribute(LoggingConstants.ATTRIBUTE_TRACE_CONTEXT, traceContext);
        TraceContextHolder.set(traceContext);
        MDC.put(LoggingConstants.MDC_TRACE_ID, traceId);
        MDC.put(LoggingConstants.MDC_DATA_SIGN, dataSign);

        log.info("Request start -> {} {} dataSign={} traceId={} ip={} ua={}", httpMethod, requestUri, dataSign, traceId, clientIp, requestWrapper.getHeader(HttpHeaders.USER_AGENT));

        try {
            // 使用包装后的请求和响应对象继续过滤链，以便后续处理器可以访问这些对象并记录请求和响应体
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 计算请求处理的持续时间，并记录请求结束日志，包括 HTTP 方法、请求 URI、响应状态码、持续时间、DataSign 和 TraceId
            long duration = System.currentTimeMillis() - startTime;
            int status = responseWrapper.getStatus();
            log.info("Request end   <- {} {} status={} duration={}ms dataSign={} traceId={}", httpMethod, requestUri, status, duration, dataSign, traceId);

            // 将响应体复制回原始响应对象，并清理 TraceContextHolder 和 MDC 中的相关属性，以避免内存泄漏
            responseWrapper.copyBodyToResponse();
            TraceContextHolder.clear();
            MDC.remove(LoggingConstants.MDC_TRACE_ID);
            MDC.remove(LoggingConstants.MDC_DATA_SIGN);
        }
    }

    /**
     * 确保提供的字符串是一个有效的标识符，如果字符串不为空且不全是空白，则返回修剪后的字符串；否则，生成一个新的 UUID 作为标识符。
     *
     * @param candidate 待验证的字符串
     * @return 有效的标识符
     */
    private String ensureIdentifier(@Nullable String candidate) {
        if (StringUtils.isNotBlank(candidate)) {
            return candidate.trim();
        }

        return IdUtil.fastSimpleUUID();
    }

    /**
     * 额外 header 支持的 ContentCachingRequestWrapper
     */
    private static final class TraceAwareRequestWrapper extends ContentCachingRequestWrapper {

        /**
         * 额外的请求头信息，用于存储自定义的请求头
         */
        private final Map<String, String> extraHeaders = new LinkedCaseInsensitiveMap<>();

        /**
         * 构造函数，初始化请求包装器并设置内容缓存限制
         *
         * @param request           原始 HTTP 请求对象
         * @param contentCacheLimit 内容缓存限制，用于指定请求体的最大缓存大小
         */
        TraceAwareRequestWrapper(HttpServletRequest request, int contentCacheLimit) {
            super(request, contentCacheLimit);
        }

        /**
         * 将指定的请求头名称和值添加到额外的请求头信息中，以便在后续的请求处理中使用
         *
         * @param name  请求头名称
         * @param value 请求头值
         */
        void putHeader(String name, String value) {
            extraHeaders.put(name, value);
        }

        /**
         * 重写 getHeader 方法，首先检查额外的请求头信息中是否存在指定名称的请求头，如果存在则返回该值；否则，调用父类的方法获取原始请求头值
         *
         * @param name 请求头名称
         * @return 请求头值
         */
        @Override
        public String getHeader(String name) {
            if (extraHeaders.containsKey(name)) {
                return extraHeaders.get(name);
            }

            return super.getHeader(name);
        }

        /**
         * 重写 getHeaders 方法，首先检查额外的请求头信息中是否存在指定名称的请求头，如果存在则将该值添加到结果列表中；然后，调用父类的方法获取原始请求头值，并将其添加到结果列表中，最后返回一个包含所有请求头值的枚举对象
         *
         * @param name 请求头名称
         * @return 请求头值枚举对象
         */
        @Override
        public Enumeration<String> getHeaders(String name) {
            if (!extraHeaders.containsKey(name)) {
                return super.getHeaders(name);
            }

            List<String> values = new ArrayList<>();
            values.add(extraHeaders.get(name));
            Enumeration<String> original = super.getHeaders(name);
            while (original.hasMoreElements()) {
                values.add(original.nextElement());
            }

            return Collections.enumeration(values);
        }

        /**
         * 重写 getHeaderNames 方法，首先获取父类的方法返回的请求头名称列表，然后将额外的请求头名称添加到该列表中，最后返回一个包含所有请求头名称的枚举对象
         *
         * @return 请求头名称枚举对象
         */
        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            extraHeaders.keySet().forEach(header -> {
                if (!names.contains(header)) {
                    names.add(header);
                }
            });

            return Collections.enumeration(names);
        }

        /**
         * 重写 getCharacterEncoding 方法，首先调用父类的方法获取原始请求的字符编码，如果该值不为 null，则返回该值；否则，返回默认的 UTF-8 字符编码名称
         *
         * @return 字符编码名称
         */
        @NullMarked
        @Override
        public String getCharacterEncoding() {
            String encoding = super.getCharacterEncoding();
            return StringUtils.isNotBlank(encoding) ? encoding : StandardCharsets.UTF_8.name();
        }
    }
}
