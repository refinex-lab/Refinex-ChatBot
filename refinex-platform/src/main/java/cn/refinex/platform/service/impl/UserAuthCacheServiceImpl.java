package cn.refinex.platform.service.impl;

import cn.refinex.core.constants.AuthRedisConstants;
import cn.refinex.platform.repository.SysPermissionRepository;
import cn.refinex.platform.repository.SysRoleRepository;
import cn.refinex.platform.service.UserAuthCacheService;
import cn.refinex.redis.core.RedisService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户认证缓存服务实现类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserAuthCacheServiceImpl implements UserAuthCacheService {

    /**
     * 缓存过期时间（30分钟）
     */
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final RedisService redisService;
    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;

    /**
     * 根据用户 ID 查询角色编码列表
     *
     * @param userId 用户 ID
     * @return 角色编码列表
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUserRoles(Long userId) {
        // 先从 Redis 缓存中获取角色列表
        String key = AuthRedisConstants.buildUserRolesKey(userId);
        List<String> cached = redisService.string().get(key, List.class);
        if (CollectionUtils.isNotEmpty(cached)) {
            return new ArrayList<>(cached);
        }

        // 缓存未命中，从数据库查询角色列表，缓存到 Redis
        List<String> roles = roleRepository.listRoleCodesByUserId(userId);
        redisService.string().set(key, roles, CACHE_TTL);
        return roles;
    }

    /**
     * 根据用户 ID 查询权限列表
     *
     * @param userId 用户 ID
     * @return 权限列表
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUserPermissions(Long userId) {
        // 先从 Redis 缓存中获取权限列表
        String key = AuthRedisConstants.buildUserPermissionsKey(userId);
        List<String> cached = redisService.string().get(key, List.class);
        if (CollectionUtils.isNotEmpty(cached)) {
            return new ArrayList<>(cached);
        }

        // 缓存未命中，从数据库查询权限列表，缓存到 Redis
        List<String> permissions = permissionRepository.listPermissionsByUserId(userId);
        redisService.string().set(key, permissions, CACHE_TTL);
        return permissions;
    }

    /**
     * 清除用户角色/权限缓存
     *
     * @param userId 用户 ID
     */
    @Override
    public void evictUserAuthCache(Long userId) {
        List<String> keys = List.of(
                AuthRedisConstants.buildUserRolesKey(userId),
                AuthRedisConstants.buildUserPermissionsKey(userId)
        );
        redisService.delete(keys);
    }
}
