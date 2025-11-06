package cn.refinex.core.api;

import org.springframework.http.HttpStatusCode;

import java.io.Serializable;

/**
 * 通用 API 状态码接口
 * <p>
 * 对齐 Spring {@link  HttpStatusCode} 语义，用于统一 API 响应中的状态码判断。
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface ApiStatusCode extends Serializable {

    /**
     * 返回数值型状态码
     * <p>
     * 示例: 200, 404, 500
     */
    int value();

    /**
     * 是否为 1xx（信息类）状态码
     *
     * @return 如果状态码在 100-199 范围内，则返回 true
     */
    default boolean is1xxInformational() {
        return value() >= 100 && value() < 200;
    }

    /**
     * 是否为 2xx（成功类）状态码
     *
     * @return 如果状态码在 200-299 范围内，则返回 true
     */
    default boolean is2xxSuccessful() {
        return value() >= 200 && value() < 300;
    }

    /**
     * 是否为 3xx（重定向类）状态码
     *
     * @return 如果状态码在 300-399 范围内，则返回 true
     */
    default boolean is3xxRedirection() {
        return value() >= 300 && value() < 400;
    }

    /**
     * 是否为 4xx（客户端错误类）状态码
     *
     * @return 如果状态码在 400-499 范围内，则返回 true
     */
    default boolean is4xxClientError() {
        return value() >= 400 && value() < 500;
    }

    /**
     * 是否为 5xx（服务端错误类）状态码
     *
     * @return 如果状态码在 500-599 范围内，则返回 true
     */
    default boolean is5xxServerError() {
        return value() >= 500 && value() < 600;
    }

    /**
     * 是否为错误状态（4xx 或 5xx）
     *
     * @return 如果状态码为 4xx 或 5xx 范围，则返回 true
     */
    default boolean isError() {
        return is4xxClientError() || is5xxServerError();
    }
}
