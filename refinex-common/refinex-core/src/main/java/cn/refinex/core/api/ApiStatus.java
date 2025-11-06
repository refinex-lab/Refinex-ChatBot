package cn.refinex.core.api;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

/**
 * 通用 API 状态枚举
 * <p>
 * 中文增强版，参考 Spring {@link HttpStatus}，并保持命名风格与 {@link ApiResponse} 对齐。
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
public enum ApiStatus implements ApiStatusCode {

    // ========== 1xx 信息类 ==========
    CONTINUE(100, Category.INFORMATIONAL, "继续请求"),
    SWITCHING_PROTOCOLS(101, Category.INFORMATIONAL, "切换协议"),

    // ========== 2xx 成功类 ==========
    OK(200, Category.SUCCESSFUL, "请求成功"),
    CREATED(201, Category.SUCCESSFUL, "资源已创建"),
    ACCEPTED(202, Category.SUCCESSFUL, "请求已接受"),
    NO_CONTENT(204, Category.SUCCESSFUL, "无返回内容"),

    // ========== 3xx 重定向类 ==========
    MOVED_PERMANENTLY(301, Category.REDIRECTION, "永久重定向"),
    FOUND(302, Category.REDIRECTION, "临时重定向"),

    // ========== 4xx 客户端错误 ==========
    BAD_REQUEST(400, Category.CLIENT_ERROR, "请求参数错误"),
    UNAUTHORIZED(401, Category.CLIENT_ERROR, "未授权"),
    FORBIDDEN(403, Category.CLIENT_ERROR, "禁止访问"),
    NOT_FOUND(404, Category.CLIENT_ERROR, "资源不存在"),
    METHOD_NOT_ALLOWED(405, Category.CLIENT_ERROR, "请求方法不被允许"),
    REQUEST_TIMEOUT(408, Category.CLIENT_ERROR, "请求超时"),
    CONFLICT(409, Category.CLIENT_ERROR, "资源冲突"),
    TOO_MANY_REQUESTS(429, Category.CLIENT_ERROR, "请求过多"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, Category.CLIENT_ERROR, "因法律原因不可用"),

    // ========== 5xx 服务端错误 ==========
    INTERNAL_SERVER_ERROR(500, Category.SERVER_ERROR, "服务器内部错误"),
    NOT_IMPLEMENTED(501, Category.SERVER_ERROR, "功能未实现"),
    BAD_GATEWAY(502, Category.SERVER_ERROR, "网关错误"),
    SERVICE_UNAVAILABLE(503, Category.SERVER_ERROR, "服务不可用"),
    GATEWAY_TIMEOUT(504, Category.SERVER_ERROR, "网关超时");

    /**
     * HTTP 状态码数值（如 200、404）
     */
    private final int value;

    /**
     * 状态类别（如 2xx 成功类）
     */
    private final Category category;

    /**
     * 状态描述（如 "请求成功"）
     */
    private final String description;

    /**
     * 构造函数，初始化 HTTP 状态码、类别和描述
     *
     * @param value       HTTP 状态码数值（如 200、404）
     * @param category    状态类别（如 2xx 成功类）
     * @param description 状态描述（如 "请求成功"）
     */
    ApiStatus(int value, Category category, String description) {
        this.value = value;
        this.category = category;
        this.description = description;
    }

    /**
     * 返回数值型状态码（如 200、404）
     */
    @Override
    public int value() {
        return this.value;
    }

    /**
     * 返回枚举的类别（类似 Series）
     */
    public Category category() {
        return this.category;
    }

    /**
     * 返回状态的字符串表示，格式为 "数值 状态名 (描述)"
     */
    @Override
    public String toString() {
        return value + " " + name() + " (" + description + ")";
    }

    /**
     * 根据数值解析枚举
     *
     * @throws IllegalArgumentException 如果找不到对应的状态
     */
    public static ApiStatus valueOf(int code) {
        ApiStatus status = resolve(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的状态码: " + code);
        }
        return status;
    }

    /**
     * 尝试解析为 ApiStatus
     */
    @Nullable
    public static ApiStatus resolve(int code) {
        for (ApiStatus status : values()) {
            if (status.value == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 状态类别，对应 HTTP 状态系列
     */
    @Getter
    public enum Category {
        INFORMATIONAL(1, "信息"),
        SUCCESSFUL(2, "成功"),
        REDIRECTION(3, "重定向"),
        CLIENT_ERROR(4, "客户端错误"),
        SERVER_ERROR(5, "服务端错误");

        /**
         * 类别数值（如 1xx 信息类的数值为 1）
         */
        private final int value;

        /**
         * 类别描述（如 "信息"、"成功"）
         */
        private final String label;

        /**
         * 构造函数，初始化类别数值和描述
         *
         * @param value 类别数值（如 1xx 信息类的数值为 1）
         * @param label 类别描述（如 "信息"、"成功"）
         */
        Category(int value, String label) {
            this.value = value;
            this.label = label;
        }

        /**
         * 根据 HTTP 状态码数值解析类别
         *
         * @param code HTTP 状态码数值（如 200、404）
         * @return 对应的状态类别（如 2xx 成功类），如果未找到则返回 null
         */
        public static @Nullable Category resolve(int code) {
            int categoryCode = code / 100;
            for (Category c : values()) {
                if (c.value == categoryCode) {
                    return c;
                }
            }
            return null;
        }
    }
}
