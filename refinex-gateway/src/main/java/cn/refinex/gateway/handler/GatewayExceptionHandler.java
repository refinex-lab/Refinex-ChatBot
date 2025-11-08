package cn.refinex.gateway.handler;

import cn.refinex.core.api.ApiResponse;
import cn.refinex.json.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关异常处理器
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@Order(-1)
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final JsonUtils jsonUtils;

    /**
     * 处理网关异常
     *
     * @param exchange 服务器 Web 交换实例
     * @param ex       抛出的异常
     * @return Mono<Void> 表示处理完成的 Mono 实例
     */
    @NullMarked
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 获取响应对象
        ServerHttpResponse response = exchange.getResponse();

        // 如果响应已提交，直接返回错误
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 提取异常信息
        String msg;
        if (ex instanceof NotFoundException) {
            msg = "服务未找到";
        } else if (ex instanceof ResponseStatusException responseStatusException) {
            msg = responseStatusException.getMessage();
        } else {
            msg = "内部服务器错误";
        }

        log.error("[网关异常处理] 请求路径：{}，异常信息：{}", exchange.getRequest().getURI(), ex.getMessage());

        // 构建响应体
        return webFluxResponseWriter(response, msg, HttpStatus.OK.value());
    }

    /**
     * 写入 WebFlux 响应体
     *
     * @param response 服务器 HTTP 响应对象
     * @param value    响应值
     * @param code     响应码
     * @return Mono<Void> 表示处理完成的 Mono 实例
     */
    @NullMarked
    private Mono<Void> webFluxResponseWriter(ServerHttpResponse response, Object value, int code) {
        // 设置响应状态码和内容类型
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // 构建响应体
        ApiResponse<?> result = ApiResponse.error(code, value.toString());
        DataBuffer dataBuffer = response.bufferFactory().wrap(jsonUtils.toJson(result).getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }
}
