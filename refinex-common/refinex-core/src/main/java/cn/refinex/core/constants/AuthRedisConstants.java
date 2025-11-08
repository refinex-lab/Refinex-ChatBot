package cn.refinex.core.constants;

import cn.refinex.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 权限 Redis 常量
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthRedisConstants {

    // ==================== 前缀定义 ====================

    /**
     * 权限系统缓存键前缀
     */
    private static final String AUTH_PREFIX = "auth:";

    /**
     * 用户拥有的角色列表缓存键前缀
     */
    private static final String USER_ROLE_LIST_PREFIX = AUTH_PREFIX + "user:{}:roles";

    /**
     * 用户拥有的权限列表缓存键前缀
     */
    private static final String USER_PERMISSION_LIST_PREFIX = AUTH_PREFIX + "user:{}:permissions";

    /**
     * 构建用户角色列表缓存键
     *
     * @param userId 用户 ID
     * @return 用户角色列表缓存键
     */
    public static String buildUserRolesKey(Long userId) {
        return StringUtils.format(USER_ROLE_LIST_PREFIX, userId);
    }

    /**
     * 构建用户权限列表缓存键
     *
     * @param userId 用户 ID
     * @return 用户权限列表缓存键
     */
    public static String buildUserPermissionsKey(Long userId) {
        return StringUtils.format(USER_PERMISSION_LIST_PREFIX, userId);
    }
}
