package cn.refinex.satoken.reactor.config;

import cn.refinex.satoken.reactor.client.AuthServiceClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

import static cn.refinex.core.constants.HttpServicesGroupConstants.REFINEX_PLATFORM_GROUP;

/**
 * 声明式注册 HTTP 服务客户端
 *
 * @author Refinex
 * @since 1.0.0
 */
@Configuration
@ImportHttpServices(
        group = REFINEX_PLATFORM_GROUP,
        clientType = HttpServiceGroup.ClientType.REST_CLIENT,
        types = {
                AuthServiceClient.class
        }
)
public class ApiClientConfig {
}
