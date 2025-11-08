package cn.refinex.gateway.filter;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.same.SaSameUtil;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局过滤器，为请求添加 Same-Token
 * <p>
 * 内部服务外网隔离: <a href="https://sa-token.cc/doc.html#/micro/same-token?id=%e5%be%ae%e6%9c%8d%e5%8a%a1-%e5%86%85%e9%83%a8%e6%9c%8d%e5%8a%a1%e5%a4%96%e7%bd%91%e9%9a%94%e7%a6%bb">...</a>
 *
 * @author Refinex
 * @since 1.0.0
 */
@Order(-100)
@Component
public class ForwardAuthFilter implements GlobalFilter {

    /**
     * 全局过滤器，为请求添加 Same-Token
     *
     * @param exchange 服务器 Web 交换
     * @param chain    网关筛选链
     * @return 服务器 Web 交换
     */
    @NullMarked
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 检查是否开启 Same-Token 校验
        Boolean checkSameToken = SaManager.getConfig().getCheckSameToken();
        // 如果未开启 Same-Token 校验，则直接放行
        if (Boolean.FALSE.equals(checkSameToken)) {
            return chain.filter(exchange);
        }

        // 如果开启 Same-Token 校验，则为请求追加 Same-Token 参数
        ServerHttpRequest newRequest = exchange
                .getRequest()
                .mutate()
                // 为请求头追加 Same-Token 参数
                .header(SaSameUtil.SAME_TOKEN, SaSameUtil.getToken())
                .build();

        // 创建新的服务器 Web 交换对象，包含追加了 Same-Token 参数的请求
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        // 放行新的服务器 Web 交换对象
        return chain.filter(newExchange);
    }
}
