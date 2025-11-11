package cn.refinex.gateway.filter;

import cn.hutool.core.map.MapUtil;
import cn.refinex.core.util.StringUtils;
import cn.refinex.gateway.config.properties.ApiDecryptProperties;
import cn.refinex.gateway.config.properties.CustomGatewayProperties;
import cn.refinex.gateway.support.GatewayRequestUtils;
import cn.refinex.json.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.LinkedHashSet;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;

/**
 * 全局日志过滤器
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalLogFilter implements GlobalFilter {

    private final ApiDecryptProperties apiDecryptProperties;
    private final CustomGatewayProperties customGatewayProperties;
    private final JsonUtils jsonUtils;

    private static final String START_TIME = "startTime";
    private static final int MAX_LOG_BODY_LENGTH = 8 * 1024;

    /**
     * 全局日志过滤器
     *
     * @param exchange 服务器 Web 交换
     * @param chain    网关筛选链
     * @return 服务器 Web 交换
     */
    @Override
    public Mono<@NonNull Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 如果没有开启请求日志，则直接放行
        if (Boolean.FALSE.equals(customGatewayProperties.getRequestLog())) {
            return chain.filter(exchange);
        }

        // 提取请求 URL
        ServerHttpRequest request = exchange.getRequest();
        String url = resolveRequestUrl(exchange, request);

        // 如果是 JSON 请求，则记录 JSON 参数
        if (GatewayRequestUtils.isJsonRequest(request)) {
            logJsonRequest(exchange, request, url);
        } else {
            // 否则记录查询参数
            logQueryParameters(request, url);
        }

        // 记录请求开始时间，请求执行完后记录执行时间差
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());

        return chain.filter(exchange)
                .doFinally(signalType -> logExecutionTime(exchange, url));
    }

    /**
     * 记录 JSON 请求参数
     *
     * @param exchange 服务器 Web 交换
     * @param request  服务器 HTTP 请求
     * @param url      请求 URL
     */
    private void logJsonRequest(ServerWebExchange exchange, ServerHttpRequest request, String url) {
        if (apiDecryptProperties.isEnabled()) {
            // 检查请求头中是否包含解密标志
            String decryptHeaderFlag = apiDecryptProperties.getHeaderFlag();
            String headerFlagValue = StringUtils.isNotBlank(decryptHeaderFlag)
                    ? request.getHeaders().getFirst(decryptHeaderFlag)
                    : null;

            // 如果包含解密标志，则记录加密参数
            if (StringUtils.isNotBlank(headerFlagValue)) {
                log.info("开始请求 => URL[{}], 参数类型[encrypt]", url);
                return;
            }
        }

        // 如果不是加密请求，则记录 JSON 参数
        String jsonParam = GatewayRequestUtils.getCachedJsonBody(exchange)
                .map(body -> GatewayRequestUtils.truncateForLog(body, MAX_LOG_BODY_LENGTH))
                .orElse(null);
        log.info("开始请求 => URL[{}], 参数类型[json], 参数:[{}]", url, jsonParam);
    }

    /**
     * 记录查询参数
     *
     * @param request 服务器 HTTP 请求
     * @param url     请求 URL
     */
    private void logQueryParameters(ServerHttpRequest request, String url) {
        MultiValueMap<@NonNull String, String> parameterMap = request.getQueryParams();
        if (MapUtil.isNotEmpty(parameterMap)) {
            String parameters = jsonUtils.toJson(parameterMap);
            log.info("开始请求 => URL[{}], 参数类型[param], 参数:[{}]", url, parameters);
        } else {
            log.info("开始请求 => URL[{}], 无参数", url);
        }
    }

    /**
     * 记录请求执行时间
     *
     * @param exchange 服务器 Web 交换
     * @param url      请求 URL
     */
    private void logExecutionTime(ServerWebExchange exchange, String url) {
        // 取出前面记录的开始时间
        Long startTime = exchange.getAttribute(START_TIME);
        if (startTime != null) {
            // 计算执行时间差
            long executeTime = System.currentTimeMillis() - startTime;
            log.info("结束请求 => URL[{}], 耗时:[{}]毫秒", url, executeTime);
        }
    }

    /**
     * 解析请求 URL
     *
     * @param exchange 服务器 Web 交换
     * @param request  服务器 HTTP 请求
     * @return 请求 URL
     */
    private String resolveRequestUrl(ServerWebExchange exchange, ServerHttpRequest request) {
        LinkedHashSet<URI> uris = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, new LinkedHashSet<>());
        URI requestUri = uris.stream().findFirst().orElse(request.getURI());
        String path = UriComponentsBuilder.fromPath(requestUri.getRawPath()).build().toUriString();
        return request.getMethod().name() + " " + path;
    }
}
