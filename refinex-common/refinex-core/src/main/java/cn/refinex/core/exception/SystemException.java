package cn.refinex.core.exception;

import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.api.ApiStatusCode;

import java.io.Serial;

/**
 * 系统异常类
 * <p>
 * 用于表示系统级错误，通常是不可预期的技术故障，例如数据库连接失败、第三方服务调用超时、
 * 文件读写异常、网络故障等。这类异常通常需要系统管理员介入处理。
 * <p>
 * 默认状态码为 500（{@link ApiStatus#INTERNAL_SERVER_ERROR}），表示服务器内部错误。
 * 系统异常的出现通常意味着需要记录详细的错误日志，便于问题排查和系统监控。
 *
 * @author Refinex
 * @since 1.0.0
 */
public class SystemException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造系统异常（使用默认状态码）
     * <p>
     * 创建系统异常时使用默认的 500 状态码和 "服务器内部错误" 消息。
     * 这是最简单的构造方式，适用于快速抛出系统异常的场景。
     */
    public SystemException() {
        super(ApiStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 构造系统异常（自定义消息）
     * <p>
     * 创建系统异常时使用默认的 500 状态码，但可以自定义错误消息，
     * 适用于需要说明具体错误原因的场景，例如 "数据库连接失败" 或 "Redis 服务不可用"。
     *
     * @param message 自定义错误消息
     */
    public SystemException(String message) {
        super(ApiStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 构造系统异常（带原因）
     * <p>
     * 创建系统异常时保留原始异常信息，便于追踪问题根源。
     * 这是推荐的构造方式，可以保留完整的异常堆栈信息，便于问题排查。
     *
     * @param message 自定义错误消息
     * @param cause   原始异常
     */
    public SystemException(String message, Throwable cause) {
        super(ApiStatus.INTERNAL_SERVER_ERROR, message, cause);
    }

    /**
     * 构造系统异常（仅带原因）
     * <p>
     * 创建系统异常时仅保留原始异常信息，错误消息使用默认的 "服务器内部错误"。
     * 适用于仅需要包装原始异常的场景。
     *
     * @param cause 原始异常
     */
    public SystemException(Throwable cause) {
        super(ApiStatus.INTERNAL_SERVER_ERROR, cause);
    }

    /**
     * 构造系统异常（使用自定义状态码）
     * <p>
     * 创建系统异常时可以指定具体的 5xx 状态码，例如 502（网关错误）、503（服务不可用）等，
     * 适用于需要更精确表达错误类型的场景。
     *
     * @param statusCode 状态码枚举（建议使用 5xx 系列状态码）
     */
    public SystemException(ApiStatusCode statusCode) {
        super(statusCode);
    }

    /**
     * 构造系统异常（自定义状态码和消息）
     * <p>
     * 创建系统异常时可以同时指定状态码和错误消息，提供最大的灵活性。
     *
     * @param statusCode 状态码枚举（建议使用 5xx 系列状态码）
     * @param message    自定义错误消息
     */
    public SystemException(ApiStatusCode statusCode, String message) {
        super(statusCode, message);
    }

    /**
     * 构造系统异常（自定义状态码、消息和原因）
     * <p>
     * 创建系统异常时提供完整的异常信息，包括状态码、错误消息和原始异常。
     * 这是最完整的构造方式，适用于需要详细记录异常信息的场景。
     *
     * @param statusCode 状态码枚举（建议使用 5xx 系列状态码）
     * @param message    自定义错误消息
     * @param cause      原始异常
     */
    public SystemException(ApiStatusCode statusCode, String message, Throwable cause) {
        super(statusCode, message, cause);
    }
}
