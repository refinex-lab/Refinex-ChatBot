package cn.refinex.platform.service;

/**
 * 登录日志记录服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface LoginAuditService {

    /**
     * 记录登录成功
     *
     * @param userId        用户 ID
     * @param username      用户名
     * @param loginIdentity 登录身份（用户名/手机号/邮箱）
     * @param loginIp       登录 IP 地址
     * @param deviceType    设备类型（PC/MOBILE）
     * @param userAgent     用户代理字符串
     */
    void recordLoginSuccess(Long userId, String username, String loginIdentity, String loginIp, String deviceType, String userAgent);

    /**
     * 记录登录失败
     *
     * @param userId        用户 ID
     * @param username      用户名
     * @param loginIdentity 登录身份（用户名/手机号/邮箱）
     * @param loginIp       登录 IP 地址
     * @param deviceType    设备类型（PC/MOBILE）
     * @param userAgent     用户代理字符串
     * @param message       登录失败消息
     */
    void recordLoginFailure(Long userId, String username, String loginIdentity, String loginIp, String deviceType, String userAgent, String message);
}
