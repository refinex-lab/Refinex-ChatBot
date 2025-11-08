package cn.refinex.core.config;

import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * HTTP 连接池配置
 *
 * @author Refinex
 * @since 1.0.0
 */
@SpringBootConfiguration
public class HttpConnectionPoolConfig {

    /**
     * 连接池配置
     *
     * @return 连接池配置
     */
    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager() {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        // 总连接数
        manager.setMaxTotal(200);
        // 每个路由的最大连接数
        manager.setDefaultMaxPerRoute(20);

        // 连接存活时间
        manager.setDefaultSocketConfig(
                SocketConfig.custom()
                        // 连接超时时间
                        .setSoTimeout(Timeout.ofSeconds(5))
                        .build()
        );

        return manager;
    }

    /**
     * HTTP 客户端配置
     *
     * @param connectionManager 连接池配置
     * @return HTTP 客户端
     */
    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager connectionManager) {
        return HttpClients.custom()
                // 连接池
                .setConnectionManager(connectionManager)
                // 共享连接池
                .setConnectionManagerShared(true)
                // 重试策略
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.ofSeconds(1)))
                // 空闲连接驱逐策略
                .evictIdleConnections(TimeValue.ofSeconds(30))
                .build();
    }
}
