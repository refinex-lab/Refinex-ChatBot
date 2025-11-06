package cn.refinex.core.api;

import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 通用 API 响应体
 * <p>
 * 这里使用 JDK 16 引入的 record 类，记录类是一种不可变的类，用于表示简单的数据传输对象（DTO）。
 * 记录类自动提供了构造函数、访问器方法、equals()、hashCode() 和 toString() 方法。
 * <p>
 * 响应体结构设计遵循 RESTful API 最佳实践，包含状态码、消息、数据和时间戳四个核心字段。
 *
 * @param <T> 响应数据的类型
 * @author Refinex
 * @since 1.0.0
 */
public record ApiResponse<T>(
        int code,
        String msg,
        @Nullable T data,
        long timestamp
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建成功响应（带数据）
     * <p>
     * 使用默认的 200 状态码和 "请求成功" 消息，适用于查询、创建、更新等操作成功后返回数据的场景。
     *
     * @param data 响应数据
     * @param <T>  响应数据的类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(@Nullable T data) {
        return of(ApiStatus.OK, data);
    }

    /**
     * 创建成功响应（无数据）
     * <p>
     * 使用默认的 200 状态码和 "请求成功" 消息，适用于删除、修改等不需要返回数据的操作。
     *
     * @param <T> 响应数据的类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 创建成功响应（自定义消息）
     * <p>
     * 使用 200 状态码，但允许自定义成功消息，适用于需要返回特定提示信息的场景。
     *
     * @param msg  自定义消息
     * @param data 响应数据
     * @param <T>  响应数据的类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(String msg, @Nullable T data) {
        return of(ApiStatus.OK.value(), msg, data);
    }

    /**
     * 创建错误响应（使用状态码枚举）
     * <p>
     * 根据提供的状态码枚举创建错误响应，消息使用枚举中定义的描述信息。
     *
     * @param status 状态码枚举
     * @param <T>    响应数据的类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(ApiStatusCode status) {
        return of(status, null);
    }

    /**
     * 创建错误响应（自定义消息）
     * <p>
     * 根据提供的状态码枚举和自定义消息创建错误响应，适用于需要返回详细错误说明的场景。
     *
     * @param status 状态码枚举
     * @param msg    自定义错误消息
     * @param <T>    响应数据的类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(ApiStatusCode status, String msg) {
        return of(status.value(), msg, null);
    }

    /**
     * 创建错误响应（使用状态码数值）
     * <p>
     * 根据提供的状态码数值和消息创建错误响应，适用于需要使用自定义状态码的场景。
     *
     * @param code 状态码数值
     * @param msg  错误消息
     * @param <T>  响应数据的类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(int code, String msg) {
        return of(code, msg, null);
    }

    /**
     * 创建响应（使用状态码枚举）
     * <p>
     * 根据提供的状态码枚举创建响应对象，消息使用枚举中定义的描述信息。
     * 这是最通用的工厂方法，适用于各种状态码的响应创建。
     *
     * @param status 状态码枚举
     * @param data   响应数据
     * @param <T>    响应数据的类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> of(ApiStatusCode status, @Nullable T data) {
        String msg = (status instanceof ApiStatus apiStatus)
                ? apiStatus.getDescription()
                : String.valueOf(status.value());
        return of(status.value(), msg, data);
    }

    /**
     * 创建响应（完全自定义）
     * <p>
     * 根据提供的状态码数值、消息和数据创建响应对象，这是最底层的工厂方法，
     * 其他工厂方法最终都会调用此方法。时间戳自动设置为当前时间。
     *
     * @param code 状态码数值
     * @param msg  响应消息
     * @param data 响应数据
     * @param <T>  响应数据的类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> of(int code, String msg, @Nullable T data) {
        return new ApiResponse<>(code, msg, data, Instant.now().toEpochMilli());
    }

    /**
     * 根据条件创建响应
     * <p>
     * 根据布尔条件判断创建成功或失败响应，适用于简单的条件判断场景。
     *
     * @param condition 条件判断结果
     * @param data      成功时返回的数据
     * @param status    失败时使用的状态码
     * @param <T>       响应数据的类型
     * @return 响应对象，条件为真返回成功响应，条件为假返回错误响应
     */
    public static <T> ApiResponse<T> condition(boolean condition, @Nullable T data, ApiStatusCode status) {
        return condition ? success(data) : error(status);
    }

    /**
     * 判断响应是否成功
     * <p>
     * 根据状态码判断响应是否为成功状态（2xx 系列状态码）。
     *
     * @return 如果状态码在 200-299 范围内，则返回 true
     */
    public boolean isSuccess() {
        return code >= 200 && code < 300;
    }

    /**
     * 判断响应是否失败
     * <p>
     * 根据状态码判断响应是否为失败状态（4xx 或 5xx 系列状态码）。
     *
     * @return 如果状态码在 400-599 范围内，则返回 true
     */
    public boolean isError() {
        return code >= 400 && code < 600;
    }
}
