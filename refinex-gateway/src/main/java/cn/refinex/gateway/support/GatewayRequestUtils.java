package cn.refinex.gateway.support;

import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.Locale;
import java.util.Optional;

/**
 * 网关请求相关的辅助方法。
 *
 * @author Refinex
 * @since 1.0.0
 */
public final class GatewayRequestUtils {

    /**
     * 缓存的 JSON 请求体在 {@link ServerWebExchange#getAttributes()} 中使用的 key。
     */
    public static final String CACHED_JSON_BODY_ATTR = GatewayRequestUtils.class.getName() + ".CACHED_JSON_BODY";

    /**
     * 私有构造函数，防止实例化。
     */
    private GatewayRequestUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 判断请求是否为 JSON 类型。
     *
     * @param request 当前请求
     * @return 如果 Content-Type 为 JSON 或兼容 JSON，则返回 true
     */
    public static boolean isJsonRequest(ServerHttpRequest request) {
        MediaType contentType = request.getHeaders().getContentType();
        if (contentType == null) {
            return false;
        }
        if (MediaType.APPLICATION_JSON.includes(contentType)
                || MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            return true;
        }
        String subtype = contentType.getSubtype();
        return subtype.toLowerCase(Locale.ROOT).endsWith("+json");
    }

    /**
     * 获取缓存的 JSON 请求体。
     *
     * @param exchange 当前请求上下文
     * @return JSON 请求体
     */
    public static Optional<String> getCachedJsonBody(ServerWebExchange exchange) {
        Object cached = exchange.getAttribute(CACHED_JSON_BODY_ATTR);
        if (cached instanceof String body) {
            return Optional.of(body);
        }
        return Optional.empty();
    }

    /**
     * 截断日志中使用的请求体，避免日志过大。
     *
     * @param body      待截断的请求体
     * @param maxLength 允许的最大长度
     * @return 截断后的请求体
     */
    public static String truncateForLog(String body, int maxLength) {
        if (body == null || body.length() <= maxLength) {
            return body;
        }
        return body.substring(0, maxLength) + "...";
    }
}
