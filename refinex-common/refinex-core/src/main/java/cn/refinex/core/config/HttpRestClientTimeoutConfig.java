package cn.refinex.core.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;

/**
 * 全局 RestClient 超时配置
 *
 * @author Refinex
 * @since 1.0.0
 */
@SpringBootConfiguration
public class HttpRestClientTimeoutConfig {

    /**
     * 配置 RestClient 超时
     *
     * @return 客户端请求工厂
     */
    @Bean
    public ClientHttpRequestFactory requestFactory() {
        // 配置连接池
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        // 最大连接数
        connectionManager.setMaxTotal(200);
        // 每个路由的最大连接数
        connectionManager.setDefaultMaxPerRoute(20);

        // 配置 HTTP 客户端
        CloseableHttpClient httpClient = HttpClients.custom()
                // 配置连接池
                .setConnectionManager(connectionManager)
                // 共享连接池
                .setConnectionManagerShared(true)
                .build();

        // 配置请求工厂
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // 连接超时 3 秒
        factory.setConnectTimeout(3000);
        // 从连接池获取连接超时 2 秒
        factory.setConnectionRequestTimeout(2000);

        return factory;
    }

    /**
     * 配置 RestClient 超时
     *
     * @param requestFactory 客户端请求工厂
     * @return RestClient 服务组配置器
     */
    @Bean
    public RestClientHttpServiceGroupConfigurer timeoutConfigurer(ClientHttpRequestFactory requestFactory) {
        return groups -> groups.forEachClient((name, builder) ->
                builder.requestFactory(requestFactory)
        );
    }
}
