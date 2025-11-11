package cn.refinex.gateway.filter;

import cn.refinex.gateway.support.GatewayRequestUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 缓存请求过滤器
 * <p>
 * 用于在 Spring Cloud Gateway 环境下缓存 JSON 请求体，解决 WebFlux 模式下请求体（body）只能被读取一次的问题。
 *
 * @author Lion Li
 * @since 1.0.0
 */
@Component
public class WebCacheRequestFilter implements WebFilter, Ordered {

    /**
     * 过滤并缓存 JSON 请求体，避免后续读取失败。
     *
     * @param exchange 当前请求上下文对象（不能为空）
     * @param chain    过滤器链（不能为空）
     * @return 异步响应结果 Mono<Void>
     */
    @NullMarked
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 获取当前请求对象, 如果不是 JSON 请求, 则直接放行，因为非 JSON 请求体无法缓存
        ServerHttpRequest request = exchange.getRequest();
        if (!GatewayRequestUtils.isJsonRequest(request)) {
            return chain.filter(exchange);
        }

        // 缓存 JSON 请求体，避免后续读取失败
        return DataBufferUtils.join(request.getBody())
                // 如果请求体为空，则创建一个空的 DataBuffer，避免后续读取失败
                .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
                // 展平 DataBuffer 为字节数组，方便后续处理
                .flatMap(dataBuffer -> {
                    byte[] bodyBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bodyBytes);
                    DataBufferUtils.release(dataBuffer);

                    // 将请求体数据转换为字符串，存储到请求属性中
                    String body = new String(bodyBytes, StandardCharsets.UTF_8);
                    exchange.getAttributes().put(GatewayRequestUtils.CACHED_JSON_BODY_ATTR, body);

                    // 创建一个装饰器请求对象，用于替换原始请求对象，将缓存后的请求体数据返回给后续处理
                    ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(request) {
                        @Override
                        public HttpHeaders getHeaders() {
                            HttpHeaders headers = new HttpHeaders();
                            // 复制原始请求头信息
                            headers.putAll(super.getHeaders());
                            // 移除 CONTENT_LENGTH 头信息，因为缓存后的请求体长度可能与原始不同
                            headers.remove(HttpHeaders.CONTENT_LENGTH);
                            // 设置新的 CONTENT_LENGTH 头信息，与缓存后的请求体长度一致
                            headers.setContentLength(bodyBytes.length);
                            return headers;
                        }

                        @Override
                        public Flux<DataBuffer> getBody() {
                            // 返回缓存后的请求体数据，确保后续处理可以读取到缓存数据
                            return Flux.defer(() ->
                                    Mono.just(exchange.getResponse().bufferFactory().wrap(bodyBytes)));
                        }
                    };

                    // 使用装饰器请求对象创建一个新的交换对象，并继续过滤器链处理
                    return chain.filter(exchange.mutate().request(decoratedRequest).build());
                });
    }

    /**
     * 获取过滤器顺序
     * <p>
     * 此处设置为 {@link Ordered#HIGHEST_PRECEDENCE} + 1，确保在大多数过滤器之前执行，以便尽早缓存请求体。
     *
     * @return 顺序值
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
