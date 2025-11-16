package cn.refinex.platform.service;

import cn.refinex.platform.controller.auth.dto.response.UserProfileDTO;
import cn.refinex.platform.controller.user.dto.request.ChangePasswordRequestDTO;
import cn.refinex.platform.controller.user.dto.request.UpdateProfileRequestDTO;

/**
 * 用户服务（个人中心）
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 获取当前登录用户的个人信息
     *
     * @return 个人信息
     */
    UserProfileDTO getProfile();

    /**
     * 更新当前登录用户的基础信息
     *
     * @param request 更新请求
     * @return 更新后的个人信息
     */
    UserProfileDTO updateProfile(UpdateProfileRequestDTO request);

    /**
     * 修改当前登录用户的密码
     *
     * @param request 修改密码请求
     */
    void changePassword(ChangePasswordRequestDTO request);
}

