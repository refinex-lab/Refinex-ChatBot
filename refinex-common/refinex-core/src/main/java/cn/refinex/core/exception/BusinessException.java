package cn.refinex.core.exception;

import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.api.ApiStatusCode;

import java.io.Serial;

/**
 * 业务异常类
 * <p>
 * 用于表示业务逻辑层面的错误，这类错误是可预期的，通常由用户的不正确操作或不满足业务规则引起，
 * 例如参数校验失败、权限不足、资源不存在、业务状态不允许操作、重复提交等。
 * <p>
 * 默认状态码为 400（{@link ApiStatus#BAD_REQUEST}），表示客户端请求错误。
 * 业务异常的错误消息应当清晰明确，便于用户理解问题所在并采取相应的操作。
 * 与系统异常不同，业务异常通常不需要记录详细的错误堆栈，只需记录必要的业务日志即可。
 *
 * @author Refinex
 * @since 1.0.0
 */
public class BusinessException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造业务异常（使用默认状态码）
     * <p>
     * 创建业务异常时使用默认的 400 状态码和 "请求参数错误" 消息。
     * 这是最简单的构造方式，适用于快速抛出业务异常的场景。
     */
    public BusinessException() {
        super(ApiStatus.BAD_REQUEST);
    }

    /**
     * 构造业务异常（自定义消息）
     * <p>
     * 创建业务异常时使用默认的 400 状态码，但可以自定义错误消息。
     * 这是最常用的构造方式，适用于大多数业务校验失败的场景，
     * 例如 "用户名已存在"、"订单状态不允许取消"、"余额不足" 等。
     *
     * @param message 自定义错误消息
     */
    public BusinessException(String message) {
        super(ApiStatus.BAD_REQUEST, message);
    }

    /**
     * 构造业务异常（带原因）
     * <p>
     * 创建业务异常时保留原始异常信息，适用于需要包装底层异常的场景。
     * 虽然业务异常通常不需要携带原因，但在某些复杂的业务处理中可能需要保留异常链。
     *
     * @param message 自定义错误消息
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(ApiStatus.BAD_REQUEST, message, cause);
    }

    /**
     * 构造业务异常（仅带原因）
     * <p>
     * 创建业务异常时仅保留原始异常信息，错误消息使用默认的 "请求参数错误"。
     * 适用于仅需要包装原始异常的场景。
     *
     * @param cause 原始异常
     */
    public BusinessException(Throwable cause) {
        super(ApiStatus.BAD_REQUEST, cause);
    }

    /**
     * 构造业务异常（使用自定义状态码）
     * <p>
     * 创建业务异常时可以指定具体的 4xx 状态码，例如 401（未授权）、403（禁止访问）、
     * 404（资源不存在）、409（资源冲突）等，适用于需要更精确表达错误类型的场景。
     *
     * @param statusCode 状态码枚举（建议使用 4xx 系列状态码）
     */
    public BusinessException(ApiStatusCode statusCode) {
        super(statusCode);
    }

    /**
     * 构造业务异常（自定义状态码和消息）
     * <p>
     * 创建业务异常时可以同时指定状态码和错误消息，提供更精确的错误信息。
     * 例如使用 401 状态码配合 "登录已过期，请重新登录" 消息，
     * 或使用 403 状态码配合 "无权访问该资源" 消息。
     *
     * @param statusCode 状态码枚举（建议使用 4xx 系列状态码）
     * @param message    自定义错误消息
     */
    public BusinessException(ApiStatusCode statusCode, String message) {
        super(statusCode, message);
    }

    /**
     * 构造业务异常（自定义状态码、消息和原因）
     * <p>
     * 创建业务异常时提供完整的异常信息，包括状态码、错误消息和原始异常。
     * 这是最完整的构造方式，适用于需要详细记录异常信息的复杂业务场景。
     *
     * @param statusCode 状态码枚举（建议使用 4xx 系列状态码）
     * @param message    自定义错误消息
     * @param cause      原始异常
     */
    public BusinessException(ApiStatusCode statusCode, String message, Throwable cause) {
        super(statusCode, message, cause);
    }
}
