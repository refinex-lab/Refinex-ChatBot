package cn.refinex.core.exception;

import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.api.ApiStatusCode;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.io.Serial;

/**
 * 基础异常类
 * <p>
 * 所有自定义异常的基类，提供统一的异常处理接口。继承自 {@link RuntimeException}，
 * 非检查异常，无需在方法签名中显式声明。
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
public class BaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码，用于标识异常类型和返回给客户端的 HTTP 状态码
     */
    private final ApiStatusCode statusCode;

    /**
     * 构造基础异常（使用状态码枚举）
     * <p>
     * 创建异常时使用预定义的状态码枚举，错误消息使用枚举中定义的描述信息。
     *
     * @param statusCode 状态码枚举
     */
    public BaseException(ApiStatusCode statusCode) {
        super(extractMessage(statusCode));
        this.statusCode = statusCode;
    }

    /**
     * 构造基础异常（自定义消息）
     * <p>
     * 创建异常时使用预定义的状态码枚举，但可以覆盖默认的错误消息，
     * 适用于需要返回更详细错误说明的场景。
     *
     * @param statusCode 状态码枚举
     * @param message    自定义错误消息
     */
    public BaseException(ApiStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * 构造基础异常（带原因）
     * <p>
     * 创建异常时保留原始异常信息，便于追踪问题根源，适用于异常包装和转换的场景。
     *
     * @param statusCode 状态码枚举
     * @param message    自定义错误消息
     * @param cause      原始异常
     */
    public BaseException(ApiStatusCode statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * 构造基础异常（仅带原因）
     * <p>
     * 创建异常时仅保留原始异常信息，错误消息使用状态码枚举中的描述信息。
     *
     * @param statusCode 状态码枚举
     * @param cause      原始异常
     */
    public BaseException(ApiStatusCode statusCode, Throwable cause) {
        super(extractMessage(statusCode), cause);
        this.statusCode = statusCode;
    }

    /**
     * 获取状态码数值
     * <p>
     * 返回状态码的数值表示（如 400、500），这是一个便捷方法，
     * 避免调用方需要通过 getStatusCode().value() 获取。
     *
     * @return 状态码数值
     */
    public int getStatusValue() {
        return statusCode.value();
    }

    /**
     * 从状态码枚举中提取消息
     * <p>
     * 如果状态码是 {@link ApiStatus} 类型，则返回其描述信息，
     * 否则返回状态码的字符串表示形式。
     *
     * @param statusCode 状态码枚举
     * @return 消息字符串
     */
    private static String extractMessage(@Nullable ApiStatusCode statusCode) {
        if (statusCode instanceof ApiStatus apiStatus) {
            return apiStatus.getDescription();
        }
        return statusCode != null ? String.valueOf(statusCode.value()) : "未知错误";
    }
}
