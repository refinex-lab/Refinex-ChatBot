package cn.refinex.gateway.handler;

import cn.refinex.core.api.ApiResponse;
import cn.refinex.json.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

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

        HttpStatus status = resolveHttpStatus(ex);
        String message = resolveMessage(ex);

        log.error("[网关异常处理] 请求路径：{}", exchange.getRequest().getURI(), ex);

        // 构建响应体
        return writeErrorResponse(response, message, status);
    }

    /**
     * 根据异常解析响应状态码。
     *
     * @param throwable 抛出的异常
     * @return {@link HttpStatus}
     */
    private HttpStatus resolveHttpStatus(Throwable throwable) {
        if (throwable instanceof NotFoundException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (throwable instanceof ResponseStatusException responseStatusException) {
            HttpStatusCode statusCode = responseStatusException.getStatusCode();
            HttpStatus resolved = HttpStatus.resolve(statusCode.value());
            return resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * 根据异常解析消息。
     *
     * @param throwable 抛出的异常
     * @return 提示消息
     */
    private String resolveMessage(Throwable throwable) {
        if (throwable instanceof NotFoundException) {
            return "服务未找到";
        }
        if (throwable instanceof ResponseStatusException responseStatusException) {
            String reason = responseStatusException.getReason();
            return reason != null ? reason : responseStatusException.getMessage();
        }
        return "内部服务器错误";
    }

    /**
     * 写入 WebFlux 响应体
     *
     * @param response 服务器 HTTP 响应对象
     * @param value    响应值
     * @param status   响应状态码
     * @return Mono<Void> 表示处理完成的 Mono 实例
     */
    @NullMarked
    private Mono<Void> writeErrorResponse(ServerHttpResponse response, Object value, HttpStatus status) {
        // 设置响应状态码和内容类型
        response.setStatusCode(status);
        HttpHeaders headers = response.getHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // 构建响应体
        ApiResponse<?> result = ApiResponse.error(status.value(), value.toString());
        byte[] bytes = jsonUtils.toJson(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(dataBuffer));
    }
}
