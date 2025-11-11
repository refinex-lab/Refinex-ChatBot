package cn.refinex.platform.service;

import cn.refinex.platform.controller.auth.dto.request.LoginRequestDTO;
import cn.refinex.platform.controller.auth.dto.request.RegisterRequestDTO;
import cn.refinex.platform.controller.auth.dto.response.LoginResponseDTO;
import cn.refinex.platform.controller.auth.dto.response.UserProfileDTO;

import java.util.List;

/**
 * 认证服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 登录响应参数
     */
    LoginResponseDTO login(LoginRequestDTO request);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 用户注册
     *
     * @param request 注册请求参数
     */
    void register(RegisterRequestDTO request);

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户
     */
    UserProfileDTO currentUser();

    /**
     * 获取用户角色列表
     *
     * @param userId 用户 ID
     * @return 用户角色列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户 ID
     * @return 用户权限列表
     */
    List<String> getUserPermissions(Long userId);
}
