package cn.refinex.core.exception.handler;

import cn.refinex.core.api.ApiResponse;
import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.exception.BaseException;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.exception.SystemException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理系统中抛出的各类异常，将异常转换为标准的 API 响应格式，确保前端能够获得统一、规范的错误信息。
 * 处理器涵盖:
 * 1. 业务异常
 * 2. 系统异常
 * 3. 参数校验异常
 * 4. HTTP 异常等
 * <p>
 * 异常处理遵循以下原则：
 * 1. 业务异常只记录简要日志，系统异常记录详细的错误堆栈信息以便排查问题；
 * 2. 对外返回的错误信息应当清晰明确，避免暴露系统内部实现细节；根据异常类型返回合适的 HTTP 状态码。
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * <p>
     * 业务异常是可预期的异常，通常由用户的不正确操作或不满足业务规则引起。
     * 对于业务异常，只记录警告级别的日志，不记录详细的堆栈信息，因为这些异常是正常的业务流程的一部分。
     * 返回的错误信息应当清晰明确，便于用户理解问题所在并采取相应的操作。
     *
     * @param e       业务异常对象
     * @param request HTTP 请求对象，用于记录请求路径等信息
     * @return 标准的 API 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: URI=[{}], Message=[{}]", request.getRequestURI(), e.getMessage());
        return ApiResponse.error(e.getStatusCode(), e.getMessage());
    }

    /**
     * 处理系统异常
     * <p>
     * 系统异常是不可预期的异常，通常是技术故障导致的，例如数据库连接失败、第三方服务调用超时等。
     * 对于系统异常，需要记录完整的错误堆栈信息以便排查问题，并且应当触发监控告警。
     * 返回给用户的错误信息可以相对模糊，避免暴露系统内部实现细节。
     *
     * @param e       系统异常对象
     * @param request HTTP 请求对象，用于记录请求路径等信息
     * @return 标准的 API 错误响应
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleSystemException(SystemException e, HttpServletRequest request) {
        log.error("系统异常: URI=[{}], Message=[{}]", request.getRequestURI(), e.getMessage(), e);
        return ApiResponse.error(e.getStatusCode(), e.getMessage());
    }

    /**
     * 处理基础异常
     * <p>
     * 处理直接抛出的 BaseException 异常，作为兜底的异常处理方法。
     * 这种情况通常不应该发生，因为应当使用更具体的 BusinessException 或 SystemException。
     *
     * @param e       基础异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应
     */
    @ExceptionHandler(BaseException.class)
    public ApiResponse<Void> handleBaseException(BaseException e, HttpServletRequest request) {
        log.error("基础异常: URI=[{}], Message=[{}]", request.getRequestURI(), e.getMessage(), e);
        return ApiResponse.error(e.getStatusCode(), e.getMessage());
    }

    /**
     * 处理方法参数校验异常（@Valid 校验失败）
     * <p>
     * 当使用 Jakarta Validation 注解（如 @NotNull、@NotBlank、@Size 等）对请求体参数进行校验时，
     * 如果校验失败会抛出 MethodArgumentNotValidException 异常。该方法会提取所有校验失败的字段和错误信息，
     * 拼接成清晰的错误提示返回给前端，便于用户定位问题。
     *
     * @param e       方法参数校验异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，包含详细的字段校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: URI=[{}], Errors=[{}]", request.getRequestURI(), errorMessage);
        return ApiResponse.error(ApiStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理绑定异常（表单数据绑定失败）
     * <p>
     * 当前端提交的表单数据无法绑定到后端的参数对象时，会抛出 BindException 异常。
     * 这通常发生在使用 @ModelAttribute 注解接收表单参数，且参数类型不匹配或校验失败时。
     * 该方法的处理逻辑与 MethodArgumentNotValidException 类似，都是提取字段错误信息并返回。
     *
     * @param e       绑定异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，包含详细的字段绑定错误信息
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBindException(BindException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: URI=[{}], Errors=[{}]", request.getRequestURI(), errorMessage);
        return ApiResponse.error(ApiStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理约束违反异常（@Validated 校验失败）
     * <p>
     * 当使用 @Validated 注解对方法参数进行校验时（例如对单个路径变量或请求参数进行校验），
     * 如果校验失败会抛出 ConstraintViolationException 异常。该方法会提取所有约束违反信息并返回。
     *
     * @param e       约束违反异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，包含详细的约束违反信息
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败: URI=[{}], Errors=[{}]", request.getRequestURI(), errorMessage);
        return ApiResponse.error(ApiStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理缺少请求参数异常
     * <p>
     * 当前端请求缺少必需的请求参数时（例如 @RequestParam 注解标记为 required=true 的参数），
     * 会抛出 MissingServletRequestParameterException 异常。该方法会明确指出缺少哪个参数。
     *
     * @param e       缺少请求参数异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明缺少的参数名称
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String errorMessage = "缺少必需的请求参数: " + e.getParameterName();
        log.warn("缺少请求参数: URI=[{}], Parameter=[{}]", request.getRequestURI(), e.getParameterName());
        return ApiResponse.error(ApiStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理方法参数类型不匹配异常
     * <p>
     * 当前端传递的参数类型与后端接口定义的参数类型不匹配时，会抛出 MethodArgumentTypeMismatchException 异常。
     * 例如接口要求传递 Long 类型的 ID，但前端传递了非数字字符串。该方法会说明参数名称和期望的类型。
     *
     * @param e       方法参数类型不匹配异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明参数类型不匹配的详细信息
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String errorMessage = String.format("参数 '%s' 类型不匹配，期望类型: %s", e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.warn("参数类型不匹配: URI=[{}], Parameter=[{}], ExpectedType=[{}]", request.getRequestURI(), e.getName(), e.getRequiredType());
        return ApiResponse.error(ApiStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理 HTTP 消息不可读异常（请求体解析失败）
     * <p>
     * 当前端传递的 JSON 格式错误，或者 JSON 中的字段类型与后端定义不匹配时，会抛出 HttpMessageNotReadableException 异常。
     * 这是一个常见的异常，通常发生在前端传递了格式错误的 JSON 数据时。
     *
     * @param e       HTTP 消息不可读异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明请求体格式错误
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体解析失败: URI=[{}], Message=[{}]", request.getRequestURI(), e.getMessage());
        return ApiResponse.error(ApiStatus.BAD_REQUEST, "请求体格式错误，请检查 JSON 格式和字段类型");
    }

    /**
     * 处理不支持的 HTTP 请求方法异常
     * <p>
     * 当前端使用的 HTTP 方法（如 GET、POST、PUT、DELETE）与接口定义不匹配时，
     * 会抛出 HttpRequestMethodNotSupportedException 异常。例如接口只支持 POST 请求，但前端发送了 GET 请求。
     *
     * @param e       不支持的请求方法异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明当前请求方法不被支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String errorMessage = String.format("不支持的请求方法: %s，该接口支持的方法: %s", e.getMethod(), String.join(", ", e.getSupportedMethods()));
        log.warn("不支持的请求方法: URI=[{}], Method=[{}]", request.getRequestURI(), e.getMethod());
        return ApiResponse.error(ApiStatus.METHOD_NOT_ALLOWED, errorMessage);
    }

    /**
     * 处理不支持的媒体类型异常
     * <p>
     * 当前端发送的 Content-Type 与接口要求的媒体类型不匹配时，会抛出 HttpMediaTypeNotSupportedException 异常。
     * 例如接口要求 application/json，但前端发送了 application/x-www-form-urlencoded。
     *
     * @param e       不支持的媒体类型异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明媒体类型不被支持
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiResponse<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        String errorMessage = String.format("不支持的媒体类型: %s，支持的媒体类型: %s", e.getContentType(), e.getSupportedMediaTypes());
        log.warn("不支持的媒体类型: URI=[{}], ContentType=[{}]", request.getRequestURI(), e.getContentType());
        return ApiResponse.error(ApiStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理访问拒绝异常
     * <p>
     * 当用户没有权限访问某个资源时，会抛出 AccessDeniedException 异常。
     * 这通常发生在 Spring Security 进行权限校验时，用户权限不足的场景。
     *
     * @param e       访问拒绝异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明访问被拒绝
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("访问被拒绝: URI=[{}], Message=[{}]", request.getRequestURI(), e.getMessage());
        return ApiResponse.error(ApiStatus.FORBIDDEN, "没有权限访问该资源");
    }

    /**
     * 处理资源未找到异常（404 - 旧版本 Spring）
     * <p>
     * 当请求的 URL 不存在时，Spring MVC 会抛出 NoHandlerFoundException 异常。
     * 需要在配置文件中设置 spring.mvc.throw-exception-if-no-handler-found=true
     * 和 spring.web.resources.add-mappings=false 才能捕获此异常。
     *
     * @param e       资源未找到异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明资源不存在
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("请求的资源不存在: URI=[{}]", request.getRequestURI());
        return ApiResponse.error(ApiStatus.NOT_FOUND, "请求的资源不存在");
    }

    /**
     * 处理资源未找到异常（404 - Spring Boot 3.x）
     * <p>
     * Spring Boot 3.x 版本中，资源未找到时抛出的是 NoResourceFoundException 异常。
     * 这是新版本 Spring 对 404 异常处理的改进，使异常处理更加清晰。
     *
     * @param e       资源未找到异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明资源不存在
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("请求的资源不存在: URI=[{}]", request.getRequestURI());
        return ApiResponse.error(ApiStatus.NOT_FOUND, "请求的资源不存在");
    }

    /**
     * 处理空指针异常
     * <p>
     * 空指针异常通常是代码缺陷导致的，不应该在生产环境中出现。当捕获到空指针异常时，
     * 应当记录详细的错误日志并及时修复代码。对外返回的错误信息应当模糊化，避免暴露内部实现。
     *
     * @param e       空指针异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明服务器内部错误
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常: URI=[{}]", request.getRequestURI(), e);
        return ApiResponse.error(ApiStatus.INTERNAL_SERVER_ERROR, "服务器内部错误，请联系管理员");
    }

    /**
     * 处理非法参数异常
     * <p>
     * 当业务代码中检测到非法参数时，可以抛出 IllegalArgumentException 异常。
     * 这是 Java 标准库提供的异常类型，用于表示参数不符合预期。
     *
     * @param e       非法参数异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明参数不合法
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数: URI=[{}], Message=[{}]", request.getRequestURI(), e.getMessage());
        return ApiResponse.error(ApiStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * 处理非法状态异常
     * <p>
     * 当业务代码中检测到非法状态时（例如订单状态不允许当前操作），可以抛出 IllegalStateException 异常。
     * 这是 Java 标准库提供的异常类型，用于表示对象状态不符合操作要求。
     *
     * @param e       非法状态异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明操作不允许
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        log.warn("非法状态: URI=[{}], Message=[{}]", request.getRequestURI(), e.getMessage());
        return ApiResponse.error(ApiStatus.CONFLICT, e.getMessage());
    }

    /**
     * 处理所有未捕获的异常
     * <p>
     * 这是最后的兜底异常处理方法，用于捕获所有未被上述方法处理的异常。
     * 任何未预期的异常都会被这个方法捕获，记录详细的错误日志，并返回统一的错误响应。
     * 确保系统不会因为未处理的异常而返回不规范的错误信息或导致系统崩溃。
     *
     * @param e       异常对象
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应，说明服务器内部错误
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("未捕获的异常: URI=[{}], Type=[{}], Message=[{}]", request.getRequestURI(), e.getClass().getName(), e.getMessage(), e);
        return ApiResponse.error(ApiStatus.INTERNAL_SERVER_ERROR, "服务器内部错误，请稍后重试");
    }

    /**
     * 格式化文件大小
     * <p>
     * 将字节大小转换为人类可读的格式（如 KB、MB、GB）。
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小字符串
     */
    private String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
