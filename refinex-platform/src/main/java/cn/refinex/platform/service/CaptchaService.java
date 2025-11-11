package cn.refinex.platform.service;

import cn.refinex.platform.controller.captcha.dto.response.CaptchaCreateResponseDTO;

/**
 * 验证码服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface CaptchaService {

    /**
     * 生成验证码
     *
     * @return 验证码生成响应
     */
    CaptchaCreateResponseDTO generate();

    /**
     * 验证验证码
     *
     * @param uuid 验证码唯一标识
     * @param code 用户输入的验证码文本
     */
    void verify(String uuid, String code);
}
