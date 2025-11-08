package cn.refinex.core.config;

import cn.refinex.core.interceptor.HttpLoggingInterceptor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;

/**
 * HTTP 拦截器 配置类
 *
 * @author Refinex
 * @since 1.0.0
 */
@SpringBootConfiguration
public class HttpInterceptorConfig {

    /**
     * 配置 HTTP 拦截器
     *
     * @return HTTP 服务组配置器
     */
    @Bean
    public RestClientHttpServiceGroupConfigurer interceptorConfigurer() {
        return groups -> groups.forEachClient((name, builder) ->
                builder
                        // 添加日志拦截器
                        .requestInterceptor(new HttpLoggingInterceptor())
        );
    }
}
