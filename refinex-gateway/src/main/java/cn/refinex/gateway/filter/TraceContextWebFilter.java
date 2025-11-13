package cn.refinex.gateway.filter;

import cn.hutool.core.util.IdUtil;
import cn.refinex.core.autoconfigure.properties.RefinexLoggingProperties;
import cn.refinex.core.logging.constants.LoggingConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.net.InetSocketAddress;

/**
 * 网关入口 Trace & DataSign 过滤器
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TraceContextWebFilter implements WebFilter, Ordered {

    private final RefinexLoggingProperties loggingProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String dataSign = obtainOrCreate(request.getHeaders().getFirst(loggingProperties.getDataSignHeader()));
        String traceId = obtainOrCreate(request.getHeaders().getFirst(loggingProperties.getTraceIdHeader()));
        long startTime = System.currentTimeMillis();

        exchange.getAttributes().put(LoggingConstants.ATTRIBUTE_REQUEST_START_TIME, startTime);
        exchange.getAttributes().put(LoggingConstants.ATTRIBUTE_DATA_SIGN, dataSign);
        exchange.getAttributes().put(LoggingConstants.ATTRIBUTE_TRACE_ID, traceId);

        response.getHeaders().set(loggingProperties.getDataSignHeader(), dataSign);
        response.getHeaders().set(loggingProperties.getTraceIdHeader(), traceId);

        String clientIp = resolveClientIp(request);
        log.info("Gateway request start -> {} {} dataSign={} traceId={} ip={}",
                request.getMethod(), request.getURI().getRawPath(), dataSign, traceId, clientIp);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(loggingProperties.getDataSignHeader(), dataSign)
                .header(loggingProperties.getTraceIdHeader(), traceId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signal -> logResult(request, signal, startTime, dataSign, traceId, clientIp));
    }

    private void logResult(ServerHttpRequest request,
                           SignalType signal,
                           long startTime,
                           String dataSign,
                           String traceId,
                           String clientIp) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("Gateway request end   <- {} {} signal={} duration={}ms dataSign={} traceId={} ip={}",
                request.getMethod(), request.getURI().getRawPath(), signal, duration, dataSign, traceId, clientIp);
    }

    private String obtainOrCreate(String value) {
        if (StringUtils.hasText(value)) {
            return value;
        }
        return IdUtil.fastSimpleUUID();
    }

    private String resolveClientIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String forwarded = headers.getFirst("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress address = request.getRemoteAddress();
        if (address != null) {
            return address.getAddress() != null ? address.getAddress().getHostAddress() : address.getHostString();
        }
        return "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
