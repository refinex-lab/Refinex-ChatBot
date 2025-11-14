package cn.refinex.gateway.filter;

import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 跨域请求过滤器
 *
 * @author Lion Li
 * @since 1.0.0
 */
@Component
public class WebCorsFilter implements WebFilter, Ordered {

    /**
     * 允许的请求头，可以根据实际情况进行调整
     */
    private static final String DEFAULT_ALLOWED_HEADERS =
            "X-Requested-With,Content-Language,Content-Type,Authorization,clientid,credential,X-XSRF-TOKEN,isToken,token,Admin-Token,App-Token,Encrypt-Key,isEncrypt";

    /**
     * 允许的请求方法，可以根据实际情况进行调整
     */
    private static final String ALLOWED_METHODS = "GET,POST,PUT,DELETE,OPTIONS,HEAD";

    /**
     * 预检请求的缓存时间，单位秒，默认 18000 秒
     */
    private static final String MAX_AGE_SECONDS = "18000";

    /**
     * 跨域请求过滤器
     *
     * @param exchange 服务器Web交换
     * @param chain    网关过滤器链
     * @return 响应 Mono
     */
    @NullMarked
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 如果是跨域请求, 则直接过滤
        if (CorsUtils.isCorsRequest(request)) {
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();

            String origin = request.getHeaders().getOrigin();
            boolean hasOrigin = StringUtils.hasText(origin);
            if (hasOrigin) {
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                headers.add(HttpHeaders.VARY, HttpHeaders.ORIGIN);
            } else {
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            }

            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);

            String requestHeaders = request.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
            String allowHeaders = StringUtils.hasText(requestHeaders) ? sanitizeHeaderValue(requestHeaders) : DEFAULT_ALLOWED_HEADERS;
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
            headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
            headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE_SECONDS);
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.toString(hasOrigin));
            headers.add(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
            headers.add(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);

            // 处理预检请求的 OPTIONS 方法，直接返回成功状态码
            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return response.setComplete();
            }
        }

        // 非跨域请求, 则继续过滤
        return chain.filter(exchange);
    }

    /**
     * 清理非法的 Header 值字符，移除换行、回车和引号，压缩多余空格
     */
    private static String sanitizeHeaderValue(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        // 移除 CR/LF 和双引号
        String cleaned = value.replace("\r", "")
                .replace("\n", "")
                .replace("\"", "");
        // 将逗号两侧空格标准化为单个逗号+单空格
        cleaned = cleaned.replaceAll("\\s*,\\s*", ",");
        // 去掉首尾空格
        return cleaned.trim();
    }

    /**
     * 获取过滤器顺序
     *
     * @return 顺序值
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
