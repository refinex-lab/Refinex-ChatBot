package cn.refinex.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * HTTP Interface 工具类
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpInterfaceUtils {

    /**
     * 创建带有默认配置的 RestClient
     *
     * @param baseUrl 基础 URL
     * @return 默认配置的 RestClient
     */
    public static RestClient createDefaultRestClient(String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(HttpStatusCode::isError,
                        (request, response) -> {
                            throw new RestClientException(
                                    "HTTP error: " + response.getStatusCode());
                        })
                .build();
    }

    /**
     * 创建带有默认配置的 HttpServiceProxyFactory
     *
     * @param restClient RestClient 实例
     * @return 默认配置的 HttpServiceProxyFactory
     */
    public static HttpServiceProxyFactory createProxyFactory(RestClient restClient) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
    }

    /**
     * 创建带有默认配置的客户端代理
     *
     * @param clientInterface 客户端接口类
     * @param baseUrl         基础 URL
     * @param <T>             客户端接口类型
     * @return 默认配置的客户端代理实例
     */
    public static <T> T createClient(Class<T> clientInterface, String baseUrl) {
        RestClient restClient = createDefaultRestClient(baseUrl);
        HttpServiceProxyFactory factory = createProxyFactory(restClient);
        return factory.createClient(clientInterface);
    }
}
