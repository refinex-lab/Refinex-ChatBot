package cn.refinex.core.config;

import cn.refinex.core.interceptor.HttpLoggingInterceptor;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;

import java.util.List;

/**
 * HTTP 拦截器 配置类
 *
 * @author Refinex
 * @since 1.0.0
 */
@SpringBootConfiguration
public class HttpInterceptorConfig {

    /**
     * 默认 HTTP 日志拦截器
     */
    @Bean
    public ClientHttpRequestInterceptor httpLoggingInterceptor() {
        return new HttpLoggingInterceptor();
    }

    /**
     * 配置 HTTP 拦截器
     *
     * @return HTTP 服务组配置器
     */
    @NullMarked
    @Bean
    public RestClientHttpServiceGroupConfigurer interceptorConfigurer(ObjectProvider<List<ClientHttpRequestInterceptor>> interceptorsProvider) {
        return groups -> groups.forEachClient((name, builder) ->
                interceptorsProvider.ifAvailable(interceptors ->
                        interceptors.forEach(builder::requestInterceptor))
        );
    }
}
