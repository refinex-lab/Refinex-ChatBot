package cn.refinex.core.config;

import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.exception.SystemException;
import cn.refinex.json.util.JsonUtils;
import cn.refinex.json.util.JsonUtilsHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP 错误处理配置类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@SpringBootConfiguration
@RequiredArgsConstructor
public class HttpErrorHandlingConfig {

    /**
     * 配置 HTTP 错误处理
     *
     * @return HTTP 服务组配置器
     */
    @Bean
    public RestClientHttpServiceGroupConfigurer errorHandlingConfigurer() {
        return groups -> groups.forEachClient((name, builder) ->
                builder.defaultStatusHandler(HttpStatusCode::is4xxClientError,
                                (request, response) -> handleClientError(response))
                        .defaultStatusHandler(HttpStatusCode::is5xxServerError,
                                (request, response) -> handleServerError(response))
        );
    }

    /**
     * 处理客户端错误
     *
     * @param response 客户端 HTTP 响应
     * @throws IOException 如果发生 I/O 错误
     */
    private void handleClientError(ClientHttpResponse response) throws IOException {
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

        HttpStatusCode status = response.getStatusCode();
        ApiStatus apiStatus = ApiStatus.valueOf(status.value());

        // 429 限流
        if (status.value() == 429) {
            log.warn("HTTP 429 限流错误：{}", parseErrorDetails(body));
            throw new BusinessException(ApiStatus.TOO_MANY_REQUESTS);
        }

        // 401/403 认证授权错误
        if (status.value() == 401 || status.value() == 403) {
            log.warn("HTTP 401/403 认证授权错误：{}", parseErrorDetails(body));
            throw new BusinessException(ApiStatus.UNAUTHORIZED);
        }

        // 400 请求错误
        if (status.value() == 400) {
            log.warn("HTTP 400 请求错误：{}", parseErrorDetails(body));
            throw new BusinessException(ApiStatus.BAD_REQUEST);
        }

        // 404 资源不存在
        if (status.value() == 404) {
            log.warn("HTTP 404 资源不存在错误：{}", parseErrorDetails(body));
            throw new BusinessException(ApiStatus.NOT_FOUND);
        }

        // 其他 4xx 错误
        if (apiStatus.is4xxClientError()) {
            log.warn("HTTP {} 客户端错误：{}", status.value(), parseErrorDetails(body));
            throw new BusinessException(apiStatus);
        }
    }

    /**
     * 处理服务器错误
     *
     * @param response 客户端 HTTP 响应
     * @throws IOException 如果发生 I/O 错误
     */
    private void handleServerError(ClientHttpResponse response) throws IOException {
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        HttpStatusCode status = response.getStatusCode();
        ApiStatus apiStatus = ApiStatus.valueOf(status.value());

        if (status.value() == 503) {
            log.warn("HTTP 503 服务不可用错误：{}", parseErrorDetails(body));
            throw new SystemException(ApiStatus.SERVICE_UNAVAILABLE);
        }

        // 其他 5xx 错误
        if (apiStatus.is5xxServerError()) {
            log.warn("HTTP {} 服务器错误：{}", status.value(), parseErrorDetails(body));
            throw new SystemException(apiStatus);
        }
    }

    /**
     * 解析错误详情
     *
     * @param body 错误响应体
     * @return 错误详情映射
     */
    private Map<String, Object> parseErrorDetails(String body) {
        try {
            JsonUtils jsonUtils = JsonUtilsHolder.get();
            return jsonUtils.fromJson(body, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Map.of("rawBody", body);
        }
    }
}
