package cn.refinex.satoken.reactor.client;

import cn.refinex.core.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import static cn.refinex.core.constants.HttpServicesGroupConstants.REFINEX_PLATFORM_GROUP;

/**
 * Platform 服务权限客户端（OpenFeign）
 * <p>
 * 使用 OpenFeign 以兼容 Spring Boot 3.x，替换 Spring Http Interface。
 */
@FeignClient(
        name = REFINEX_PLATFORM_GROUP,
        contextId = "authServiceClient",
        path = "/auth"
)
public interface AuthServiceClient {

    /**
     * 获取用户权限列表
     */
    @GetMapping("/permissions/{userId}")
    ApiResponse<List<String>> getUserPermissions(@PathVariable("userId") Long userId);

    /**
     * 获取用户角色列表
     */
    @GetMapping("/roles/{userId}")
    ApiResponse<List<String>> getUserRoles(@PathVariable("userId") Long userId);
}
