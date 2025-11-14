package cn.refinex.satoken.common.exception.handler;

import cn.dev33.satoken.exception.*;
import cn.refinex.core.api.ApiResponse;
import cn.refinex.core.api.ApiStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 异常统一处理器
 * <p>
 * 专门处理 Sa-Token 框架抛出的各类认证授权异常,将其转换为统一的 API 响应格式。
 * <p>
 * 该处理器会捕获以下类型的异常:
 * 1. NotLoginException - 未登录异常(包含多种未登录场景)
 * 2. NotPermissionException - 权限不足异常
 * 3. NotRoleException - 角色不足异常
 * 4. DisableServiceException - 服务封禁异常
 * 5. SaTokenException - Sa-Token 其他异常
 * <p>
 * 异常处理原则:
 * 1. 认证授权异常属于可预期的业务异常,只记录 warn 级别日志
 * 2. 返回清晰明确的错误信息,帮助前端正确处理
 * 3. 避免暴露系统内部实现细节
 * 4. 统一的响应格式,便于前端统一处理
 * <p>
 * 使用 @Order(-100) 确保在全局异常处理器之前执行
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Order(-100)
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
public class SaTokenExceptionHandler {

    /**
     * 处理未登录异常
     * <p>
     * NotLoginException 包含多种未登录的场景, 通过 getType() 可以区分:
     * - NOT_TOKEN: 未提供 Token
     * - INVALID_TOKEN: Token 无效
     * - TOKEN_TIMEOUT: Token 已过期
     * - BE_REPLACED: Token 已被顶替(同账号登录)
     * - KICK_OUT: 账号被踢下线
     * <p>
     * 根据不同的场景返回不同的错误信息, 便于前端做相应处理。
     *
     * @param e       未登录异常
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应
     */
    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        // 根据不同的未登录类型,返回不同的提示信息
        String message = switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "未提供登录凭证,请先登录";
            case NotLoginException.INVALID_TOKEN -> "登录凭证无效,请重新登录";
            case NotLoginException.TOKEN_TIMEOUT -> "登录已过期,请重新登录";
            case NotLoginException.BE_REPLACED -> "您的账号已在其他设备登录,如非本人操作请及时修改密码";
            case NotLoginException.KICK_OUT -> "您已被强制下线,请联系管理员";
            default -> "未登录或登录已失效,请重新登录";
        };

        log.warn("未登录异常: URI=[{}], Type=[{}], Message=[{}]", request.getRequestURI(), e.getType(), message);
        // 返回 401 Unauthorized 状态码
        return ApiResponse.error(ApiStatus.UNAUTHORIZED, message);
    }

    /**
     * 处理权限不足异常
     * <p>
     * 当用户试图访问没有权限的资源时抛出此异常。
     * 异常中包含了需要的权限信息, 可以记录下来便于审计。
     *
     * @param e       权限不足异常
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应
     */
    @ExceptionHandler(NotPermissionException.class)
    public ApiResponse<Void> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        String message = "权限不足,无法访问该资源";
        // 记录详细的权限信息,便于问题排查和安全审计
        log.warn("权限不足: URI=[{}], RequiredPermission=[{}]", request.getRequestURI(), e.getPermission());
        // 返回 403 Forbidden 状态码
        return ApiResponse.error(ApiStatus.FORBIDDEN, message);
    }

    /**
     * 处理角色不足异常
     * <p>
     * 当用户试图访问需要特定角色才能访问的资源时抛出此异常。
     * 与权限不足类似, 但是基于角色的粗粒度控制。
     *
     * @param e       角色不足异常
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应
     */
    @ExceptionHandler(NotRoleException.class)
    public ApiResponse<Void> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        String message = "角色权限不足,无法访问该资源";
        // 记录详细的角色信息
        log.warn("角色不足: URI=[{}], RequiredRole=[{}]", request.getRequestURI(), e.getRole());
        // 返回 403 Forbidden 状态码
        return ApiResponse.error(ApiStatus.FORBIDDEN, message);
    }

    /**
     * 处理服务封禁异常
     * <p>
     * 当账号被封禁时抛出此异常。封禁可能是临时的或永久的。
     * 异常中包含了封禁的剩余时间信息。
     *
     * @param e       服务封禁异常
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应
     */
    @ExceptionHandler(DisableServiceException.class)
    public ApiResponse<Void> handleDisableServiceException(DisableServiceException e, HttpServletRequest request) {
        String message;
        long disableTime = e.getDisableTime();

        if (disableTime == -1) {
            // 永久封禁
            message = "您的账号已被永久封禁,请联系管理员";
        } else {
            // 临时封禁, 计算剩余时间
            long minutes = disableTime / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                message = String.format("您的账号已被封禁,剩余 %d 天后解封", days);
            } else if (hours > 0) {
                message = String.format("您的账号已被封禁,剩余 %d 小时后解封", hours);
            } else if (minutes > 0) {
                message = String.format("您的账号已被封禁,剩余 %d 分钟后解封", minutes);
            } else {
                message = String.format("您的账号已被封禁,剩余 %d 秒后解封", disableTime);
            }
        }

        log.warn("账号被封禁: URI=[{}], Service=[{}], DisableTime=[{}]", request.getRequestURI(), e.getService(), disableTime);
        // 返回 403 Forbidden 状态码
        return ApiResponse.error(ApiStatus.FORBIDDEN, message);
    }

    /**
     * 处理 Sa-Token 其他异常
     * <p>
     * 捕获所有未被上述方法处理的 Sa-Token 异常, 作为兜底处理。
     * 这类异常通常是框架内部错误或配置错误。
     *
     * @param e       Sa-Token 异常
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应
     */
    @ExceptionHandler(SaTokenException.class)
    public ApiResponse<Void> handleSaTokenException(SaTokenException e, HttpServletRequest request) {
        String message = "认证授权失败: " + e.getMessage();
        // 记录详细的错误信息,便于排查问题
        log.error("Sa-Token 异常: URI=[{}], Message=[{}]", request.getRequestURI(), e.getMessage(), e);
        // 返回 500 Internal Server Error 状态码
        return ApiResponse.error(ApiStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 处理 Sa-Token 二级认证异常
     * <p>
     * 当执行敏感操作需要二次认证时, 如果未通过二次认证则抛出此异常。
     *
     * @param e       二级认证异常
     * @param request HTTP 请求对象
     * @return 标准的 API 错误响应
     */
    @ExceptionHandler(NotSafeException.class)
    public ApiResponse<Void> handleNotSafeException(NotSafeException e, HttpServletRequest request) {
        String message = "需要进行二次认证才能执行此操作";
        log.warn("二级认证失败: URI=[{}]", request.getRequestURI());
        // 返回 403 Forbidden 状态码
        return ApiResponse.error(ApiStatus.FORBIDDEN, message);
    }
}
