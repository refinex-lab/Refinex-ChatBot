package cn.refinex.core.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * 拦截 HTTP 请求并记录日志
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
public class HttpLoggingInterceptor implements ClientHttpRequestInterceptor, Ordered {

    /**
     * 拦截 HTTP 请求并记录日志
     *
     * @param request   HTTP 请求对象
     * @param body      HTTP 请求体
     * @param execution HTTP 请求执行器
     * @return HTTP 响应对象
     * @throws IOException 如果发生 I/O 错误
     */
    @NullMarked
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long startTime = System.currentTimeMillis();

        log.info("Http Request: {} {}", request.getMethod(), request.getURI());
        log.debug("Http Headers: {}", request.getHeaders());

        ClientHttpResponse response = execution.execute(request, body);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Http Response: {} in {}ms", response.getStatusCode(), duration);

        return response;
    }

    /**
     * 获取拦截器的执行顺序，返回最低优先级
     *
     * @return 执行顺序
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
