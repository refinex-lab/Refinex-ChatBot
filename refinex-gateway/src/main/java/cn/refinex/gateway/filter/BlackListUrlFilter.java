package cn.refinex.gateway.filter;

import cn.refinex.core.api.ApiResponse;
import cn.refinex.core.api.ApiStatus;
import cn.refinex.json.util.JsonUtils;
import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 黑名单 URL 过滤
 *
 * @author ruoyi
 * @author Refinex
 * @since 1.0.0
 */
@Component
public class BlackListUrlFilter extends AbstractGatewayFilterFactory<BlackListUrlFilter.Config> {

    private final JsonUtils jsonUtils;

    /**
     * 构造函数，初始化配置类
     */
    public BlackListUrlFilter(JsonUtils jsonUtils) {
        super(Config.class);
        this.jsonUtils = jsonUtils;
    }

    /**
     * 应用黑名单 URL 过滤
     *
     * @param config 配置参数
     * @return 网关过滤器实例
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String url = exchange.getRequest().getURI().getPath();

            // 检查 URL 是否匹配黑名单中的正则表达式
            if (config.matchBlacklist(url)) {
                // 如果匹配到黑名单中的 URL，则返回 403 错误响应
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.FORBIDDEN);
                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

                ApiResponse<?> result = ApiResponse.error(ApiStatus.FORBIDDEN, "请求地址不允许访问");
                byte[] bytes = jsonUtils.toJson(result).getBytes(StandardCharsets.UTF_8);
                DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);
                return response.writeWith(Mono.just(dataBuffer));
            }

            return chain.filter(exchange);
        };
    }

    /**
     * 黑名单 URL 配置类
     */
    @Data
    public static class Config {

        /**
         * 黑名单 URL 列表
         */
        private List<String> blacklistUrl = new ArrayList<>();

        /**
         * 编译后的黑名单 URL 正则表达式列表
         */
        private final List<Pattern> blacklistUrlPattern = new ArrayList<>();

        /**
         * 设置黑名单 URL 列表
         *
         * @param blacklistUrl 黑名单 URL 列表
         */
        public void setBlacklistUrl(List<String> blacklistUrl) {
            this.blacklistUrl = CollectionUtils.isEmpty(blacklistUrl)
                    ? new ArrayList<>()
                    : new ArrayList<>(blacklistUrl);
            this.blacklistUrlPattern.clear();
            this.blacklistUrl.forEach(url -> this.blacklistUrlPattern.add(
                    Pattern.compile(url.replace("**", "(.*?)"), Pattern.CASE_INSENSITIVE)
            ));
        }

        /**
         * 检查 URL 是否匹配黑名单中的正则表达式
         *
         * @param url 请求路径
         * @return 如果 URL 匹配黑名单中的正则表达式，则返回 true；否则返回 false
         */
        public boolean matchBlacklist(String url) {
            return !blacklistUrlPattern.isEmpty()
                    && blacklistUrlPattern.stream().anyMatch(p -> p.matcher(url).find());
        }
    }
}
