package cn.refinex.platform.service;

import java.util.List;

/**
 * 用户角色/权限缓存服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface UserAuthCacheService {

    /**
     * 根据用户 ID 查询角色编码列表
     *
     * @param userId 用户 ID
     * @return 角色编码列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 根据用户 ID 查询权限列表
     *
     * @param userId 用户 ID
     * @return 权限列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 清除用户角色/权限缓存
     *
     * @param userId 用户 ID
     */
    void evictUserAuthCache(Long userId);
}
