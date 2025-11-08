package cn.refinex.satoken.reactor.client;

import cn.refinex.core.api.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * Platform 服务权限客户端
 *
 * @author Refinex
 * @since 1.0.0
 */
@HttpExchange(value = "/auth", accept = MediaType.APPLICATION_JSON_VALUE)
public interface AuthServiceClient {

    /**
     * 获取用户权限列表
     */
    @GetExchange("/permissions/{userId}")
    ApiResponse<List<String>> getUserPermissions(@PathVariable("userId") Long userId);

    /**
     * 获取用户角色列表
     */
    @GetExchange("/roles/{userId}")
    ApiResponse<List<String>> getUserRoles(@PathVariable("userId") Long userId);
}
