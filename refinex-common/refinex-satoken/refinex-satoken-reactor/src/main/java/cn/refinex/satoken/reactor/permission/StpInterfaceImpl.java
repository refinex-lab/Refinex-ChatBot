package cn.refinex.satoken.reactor.permission;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.convert.Convert;
import cn.refinex.core.api.ApiResponse;
import cn.refinex.core.constants.AuthRedisConstants;
import cn.refinex.core.util.StringUtils;
import cn.refinex.json.util.JsonUtils;
import cn.refinex.redis.core.RedisService;
import cn.refinex.satoken.common.helper.LoginHelper;
import cn.refinex.satoken.common.model.LoginUser;
import cn.refinex.satoken.reactor.client.AuthServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Sa-Token 权限接口实现
 * <p>
 * 此接口用于 Sa-Token 获取用户的权限和角色信息。
 * 在需要进行权限校验时(如 @SaCheckPermission 注解),
 * Sa-Token 会调用这些方法获取当前用户的权限和角色。
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {

    private final AuthServiceClient platformAuthClient;
    private final RedisService redisService;
    private final JsonUtils jsonUtils;

    /**
     * 构造函数
     *
     * @param platformAuthClient 平台认证客户端
     * @param redisService       Redis 服务
     * @param jsonUtils          JSON 工具类
     */
    public StpInterfaceImpl(AuthServiceClient platformAuthClient, RedisService redisService, JsonUtils jsonUtils) {
        this.platformAuthClient = platformAuthClient;
        this.redisService = redisService;
        this.jsonUtils = jsonUtils;
    }

    /**
     * 返回指定用户的权限列表
     *
     * @param loginId   用户ID
     * @param loginType 登录类型
     * @return 权限编码列表
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getPermissionList(Object loginId, String loginType) {
        log.debug("获取用户权限列表: loginId={}, loginType={}", loginId, loginType);

        // 获取当前登录用户
        LoginUser loginUser = LoginHelper.getLoginUser();
        // 获取实际用户ID
        Long userId = Convert.toLong(loginId);

        // 如果当前登录用户为空或用户 ID 不匹配, 则重新获取用户权限
        if (Objects.isNull(loginUser) || !loginUser.getUserId().equals(userId)) {
            // 如果是超级管理员，返回所有权限
            if (LoginHelper.isSuperAdmin(userId)) {
                return List.of("*:*:*");
            }

            // 如果不是超级管理员，先尝试从 Redis 中获取用户权限
            String userPermissionsKey = AuthRedisConstants.buildUserPermissionsKey(userId);
            String permissions = redisService.string().get(userPermissionsKey, String.class);
            if (StringUtils.isNoneBlank(permissions)) {
                // 缓存里面存储的是 JSON 字符串，需要解析
                return jsonUtils.fromJson(permissions, List.class);
            }

            // 如果缓存没有，则调用平台接口获取用户权限(重写缓存在内部实现)
            ApiResponse<List<String>> apiResponse = platformAuthClient.getUserPermissions(userId);
            if (apiResponse.isError()) {
                log.error("获取用户权限失败: userId={}, apiResponse={}", userId, apiResponse);
                return List.of();
            }
            return apiResponse.data();
        }

        // 如果当前登录用户不为空且用户 ID 匹配, 则直接返回用户权限
        return new ArrayList<>(LoginHelper.getPermissions());
    }

    /**
     * 返回指定用户的角色列表
     *
     * @param loginId   用户ID
     * @param loginType 登录类型
     * @return 角色编码列表
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getRoleList(Object loginId, String loginType) {
        log.debug("获取用户角色列表: loginId={}, loginType={}", loginId, loginType);

        // 获取当前登录用户
        LoginUser loginUser = LoginHelper.getLoginUser();
        // 获取实际用户ID
        Long userId = Convert.toLong(loginId);

        // 如果当前登录用户为空或用户 ID 不匹配, 则重新获取用户角色
        if (Objects.isNull(loginUser) || !loginUser.getUserId().equals(userId)) {
            // 如果是超级管理员，返回所有角色
            if (LoginHelper.isSuperAdmin(userId)) {
                return List.of("*:*:*");
            }

            // 如果不是超级管理员，先尝试从 Redis 中获取用户角色
            String userRolesKey = AuthRedisConstants.buildUserRolesKey(userId);
            String roles = redisService.string().get(userRolesKey, String.class);
            if (StringUtils.isNoneBlank(roles)) {
                // 缓存里面存储的是 JSON 字符串，需要解析
                return jsonUtils.fromJson(roles, List.class);
            }

            // 如果缓存没有，则调用平台接口获取用户角色(重写缓存在内部实现)
            ApiResponse<List<String>> apiResponse = platformAuthClient.getUserRoles(userId);
            if (apiResponse.isError()) {
                log.error("获取用户角色失败: userId={}, apiResponse={}", userId, apiResponse);
                return List.of();
            }
            return apiResponse.data();
        }

        // 如果当前登录用户不为空且用户 ID 匹配, 则直接返回用户角色
        return new ArrayList<>(LoginHelper.getRoles());
    }
}
