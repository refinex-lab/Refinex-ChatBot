package cn.refinex.core.logging.aspect;

import cn.refinex.core.autoconfigure.properties.RefinexLoggingProperties;
import cn.refinex.core.logging.annotation.RequestLog;
import cn.refinex.core.logging.constants.LoggingConstants;
import cn.refinex.core.logging.context.TraceContext;
import cn.refinex.core.logging.context.TraceContextHolder;
import cn.refinex.core.logging.handler.RequestLogHandler;
import cn.refinex.core.logging.model.RequestLogEntry;
import cn.refinex.core.logging.user.RequestUser;
import cn.refinex.core.logging.user.RequestUserExtractor;
import cn.refinex.core.util.ServletUtils;
import cn.refinex.core.util.StringUtils;
import cn.refinex.json.util.JsonUtils;
import cn.refinex.json.util.JsonUtilsHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 请求日志切面
 *
 * @author Refinex
 * @since 1.0.0
 */
@Aspect
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 50) // 确保在 TraceLoggingFilter 之后执行，优先级高于其他日志切面
public class RequestLogAspect {

    private final RequestLogHandler requestLogHandler;
    private final RefinexLoggingProperties properties;
    private final String serviceName;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final JsonUtils jsonUtils = JsonUtilsHolder.get();

    /**
     * 构造函数，初始化请求日志处理器、日志配置属性和服务名称
     *
     * @param requestLogHandler 请求日志处理器
     * @param properties        日志配置属性
     * @param environment       环境变量
     */
    public RequestLogAspect(RequestLogHandler requestLogHandler, RefinexLoggingProperties properties, Environment environment) {
        this.requestLogHandler = requestLogHandler;
        this.properties = properties;
        this.serviceName = environment.getProperty("spring.application.name", "refinex-service");
    }

    /**
     * 环绕通知，拦截所有标注了 @RequestLog 注解的方法，记录请求日志并处理异常
     *
     * @param joinPoint  连接点
     * @param requestLog 请求日志注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(requestLog)")
    public Object around(ProceedingJoinPoint joinPoint, RequestLog requestLog) throws Throwable {
        // 获取当前 HTTP 请求，如果没有则直接执行方法
        HttpServletRequest request = ServletUtils.getRequest();
        if (Objects.isNull(request) || shouldSkip(request)) {
            return joinPoint.proceed();
        }

        // 获取当前 HTTP 响应，如果没有则设为 null
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = null;
        if (attrs instanceof ServletRequestAttributes servletRequestAttributes) {
            response = servletRequestAttributes.getResponse();
        }

        // 记录开始时间，执行方法，并捕获异常
        long start = System.currentTimeMillis();
        boolean success = true;
        Throwable throwable = null;
        Object result = null;

        try {
            // 执行目标方法并获取结果
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            success = false;
            throwable = ex;
            throw ex;
        } finally {
            try {
                // 记录请求日志，包括请求信息、响应信息、执行结果和异常信息，并计算执行时间
                persistLog(joinPoint, requestLog, request, response, result, success, throwable, System.currentTimeMillis() - start);
            } catch (Exception logEx) {
                log.warn("Failed to persist request log: {}", logEx.getMessage());
            }
        }
    }

    /**
     * 判断是否应该跳过记录日志，根据请求 URI 是否在忽略路径列表中
     *
     * @param request 当前 HTTP 请求
     * @return 如果请求 URI 在忽略路径列表中，则返回 true；否则返回 false
     */
    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return properties.getRequestLog().getIgnorePaths().stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, uri));
    }

    /**
     * 持久化请求日志，构建日志实体并调用日志处理器进行处理
     *
     * @param joinPoint  连接点
     * @param requestLog 请求日志注解
     * @param request    当前 HTTP 请求
     * @param response   当前 HTTP 响应（如果有）
     * @param result     方法执行结果（如果有）
     * @param success    方法是否执行成功
     * @param throwable  方法执行异常（如果有）
     * @param duration   方法执行时间（毫秒）
     */
    private void persistLog(ProceedingJoinPoint joinPoint, RequestLog requestLog, HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object result, boolean success, @Nullable Throwable throwable, long duration) {
        // 获取请求日志配置属性，并判断是否需要持久化日志
        RefinexLoggingProperties.RequestLogProperties requestLogProps = properties.getRequestLog();
        boolean persistEnabled = requestLogProps.isPersist() && requestLog.persist();

        // 从请求属性中获取 TraceContext，如果没有则从 TraceContextHolder 中获取
        TraceContext traceContext = (TraceContext) request.getAttribute(LoggingConstants.ATTRIBUTE_TRACE_CONTEXT);
        if (traceContext == null) {
            traceContext = TraceContextHolder.get();
        }

        // 根据配置决定是否记录请求体和响应体，并提取相应内容
        String requestBody = null;
        if (requestLog.recordRequestBody() && requestLogProps.isRecordRequestBody()) {
            requestBody = extractRequestBody(request, requestLogProps.getBodyMaxLength());
        }
        String responseBody = null;
        if (requestLog.recordResponseBody() && requestLogProps.isRecordResponseBody()) {
            responseBody = convertResponse(result, requestLogProps.getBodyMaxLength());
        }

        // 获取当前用户信息，如果没有则设为 null
        RequestUser requestUser = RequestUserExtractor.currentUser().orElse(null);

        // 构建请求日志实体，包含服务名称、标题、类型、描述、请求 URI、HTTP 方法、客户端 IP、用户代理、数据签名、TraceId、HTTP 状态码、执行结果、用户信息、控制器和方法信息、请求体和响应体等信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequestLogEntry entry = RequestLogEntry.builder()
                .serviceName(serviceName)
                .title(resolveTitle(requestLog, method))
                .type(requestLog.type())
                .description(requestLog.description())
                .requestUri(request.getRequestURI())
                .httpMethod(request.getMethod())
                .clientIp(ServletUtils.getClientIp(request))
                .userAgent(request.getHeader(HttpHeaders.USER_AGENT))
                .dataSign(resolveDataSign(traceContext))
                .traceId(resolveTraceId(traceContext))
                .httpStatus(Objects.nonNull(response) ? response.getStatus() : null)
                .success(success)
                .userId(Objects.nonNull(requestUser) ? requestUser.userId() : null)
                .username(Objects.nonNull(requestUser) ? requestUser.username() : null)
                .controller(signature.getDeclaringTypeName())
                .methodName(method.getName())
                .requestBody(requestBody)
                .responseBody(responseBody)
                .errorMessage(Objects.nonNull(throwable) ? throwable.getMessage() : null)
                .durationMs(duration)
                .timestamp(System.currentTimeMillis())
                .build();

        // 调用日志处理器处理日志实体，如果启用了持久化，则将日志保存到数据库中
        requestLogHandler.handle(entry, persistEnabled);
    }

    /**
     * 解析请求日志标题，优先使用注解中的标题，否则使用默认格式（类名#方法名）
     *
     * @param requestLog 请求日志注解
     * @param method     被注解的方法
     * @return 解析后的标题
     */
    private String resolveTitle(RequestLog requestLog, Method method) {
        if (StringUtils.isNotBlank(requestLog.title())) {
            return requestLog.title();
        }

        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }

    /**
     * 解析数据签名，优先使用 TraceContext 中的数据签名，如果没有则从 MDC 中获取
     *
     * @param context TraceContext 对象
     * @return 数据签名字符串
     */
    private String resolveDataSign(@Nullable TraceContext context) {
        if (context != null && StringUtils.isNotBlank(context.dataSign())) {
            return context.dataSign();
        }

        return MDC.get(LoggingConstants.MDC_DATA_SIGN);
    }

    /**
     * 解析 TraceId，优先使用 TraceContext 中的 TraceId，如果没有则从 MDC 中获取
     *
     * @param context TraceContext 对象
     * @return TraceId 字符串
     */
    private String resolveTraceId(@Nullable TraceContext context) {
        if (context != null && StringUtils.isNotBlank(context.traceId())) {
            return context.traceId();
        }

        return MDC.get(LoggingConstants.MDC_TRACE_ID);
    }

    /**
     * 提取请求体内容，优先使用 ContentCachingRequestWrapper 中的内容，如果没有则返回 null
     *
     * @param request   当前 HTTP 请求
     * @param maxLength 请求体内容的最大长度
     * @return 请求体内容字符串，或者 null 如果没有内容
     */
    @Nullable
    private String extractRequestBody(HttpServletRequest request, int maxLength) {
        ContentCachingRequestWrapper wrapper = null;
        if (request instanceof ContentCachingRequestWrapper cachingRequestWrapper) {
            wrapper = cachingRequestWrapper;
        } else {
            Object attr = request.getAttribute(LoggingConstants.ATTRIBUTE_REQUEST_WRAPPER);
            if (attr instanceof ContentCachingRequestWrapper caching) {
                wrapper = caching;
            }
        }

        if (wrapper == null) {
            return null;
        }

        byte[] buf = wrapper.getContentAsByteArray();
        if (buf.length == 0) {
            return null;
        }

        wrapper.getCharacterEncoding();
        String charset = wrapper.getCharacterEncoding();
        String payload = new String(buf, 0, buf.length, Charset.forName(charset));
        return maskSensitiveFields(StringUtils.abbreviate(payload, maxLength));
    }

    /**
     * 转换响应体内容为 JSON 字符串，优先使用 JsonUtils 转换，如果没有则返回 null
     *
     * @param result   响应体对象
     * @param maxLength 响应体内容的最大长度
     * @return 转换后的 JSON 字符串，或者 null 如果没有内容
     */
    @Nullable
    private String convertResponse(@Nullable Object result, int maxLength) {
        if (result == null) {
            return null;
        }

        String payload = jsonUtils.toJson(result);
        return StringUtils.abbreviate(payload, maxLength);
    }

    /**
     * 对敏感字段进行脱敏处理，将指定字段的值替换为 "***"
     *
     * @param payload 原始字符串
     * @return 脱敏后的字符串
     */
    private String maskSensitiveFields(@Nullable String payload) {
        if (StringUtils.isBlank(payload)) {
            return payload;
        }

        String masked = payload;

        for (String field : properties.getRequestLog().getSensitiveFields()) {
            String regex = "(\"" + Pattern.quote(field) + "\"\\s*:\\s*)(\".*?\"|\\d+|true|false|null)";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(masked);

            StringBuilder buffer = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(buffer, matcher.group(1) + "\"***\"");
            }

            matcher.appendTail(buffer);
            masked = buffer.toString();
        }

        return masked;
    }
}
