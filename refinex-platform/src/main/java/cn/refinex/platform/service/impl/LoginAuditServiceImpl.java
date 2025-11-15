package cn.refinex.platform.service.impl;

import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.platform.entity.SysLoginLog;
import cn.refinex.platform.repository.SysLoginLogRepository;
import cn.refinex.platform.repository.SysUserRepository;
import cn.refinex.platform.service.LoginAuditService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录审计服务实现类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAuditServiceImpl implements LoginAuditService {

    private final SysLoginLogRepository loginLogRepository;
    private final SysUserRepository userRepository;
    private final JdbcTemplateManager jdbcManager;

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
    @Async("loginLogExecutor")
    @Override
    public void
    recordLoginSuccess(Long userId, String username, String loginIdentity, String loginIp, String deviceType, String userAgent) {
        LocalDateTime now = LocalDateTime.now();
        LoginEvent event = LoginEvent.builder()
                .userId(userId)
                .username(username)
                .loginIdentity(loginIdentity)
                .loginIp(loginIp)
                .deviceType(deviceType)
                .userAgent(userAgent)
                .status(1)
                .message("登录成功")
                .loginTime(now)
                .build();

        SysLoginLog logEntity = buildLog(event);
        jdbcManager.executeInTransaction(jdbc -> {
            loginLogRepository.insert(logEntity, jdbc);
            if (userId != null) {
                userRepository.updateLastLoginInfo(userId, now, loginIp, jdbc);
            }
            return null;
        });
        log.debug("登录成功日志已记录 userId={}, identity={}", userId, loginIdentity);
    }

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
    @Async("loginLogExecutor")
    @Override
    public void recordLoginFailure(Long userId, String username, String loginIdentity, String loginIp, String deviceType, String userAgent, String message) {
        LocalDateTime now = LocalDateTime.now();
        LoginEvent event = LoginEvent.builder()
                .userId(userId)
                .username(username)
                .loginIdentity(loginIdentity)
                .loginIp(loginIp)
                .deviceType(deviceType)
                .userAgent(userAgent)
                .status(0)
                .message(message)
                .loginTime(now)
                .build();

        SysLoginLog logEntity = buildLog(event);
        jdbcManager.executeInTransaction(jdbc -> {
            loginLogRepository.insert(logEntity, jdbc);
            return null;
        });
        log.warn("登录失败已记录 identity={}, message={}", loginIdentity, message);
    }

    /**
     * 构建登录日志实体
     */
    private SysLoginLog buildLog(LoginEvent event) {
        LocalDateTime now = LocalDateTime.now();
        return SysLoginLog.builder()
                .userId(event.getUserId())
                .username(event.getUsername())
                .loginIdentity(event.getLoginIdentity())
                .status(event.getStatus())
                .message(event.getMessage())
                .loginIp(event.getLoginIp())
                .deviceType(event.getDeviceType())
                .userAgent(event.getUserAgent())
                .loginTime(event.getLoginTime())
                .createBy(event.getUserId())
                .createTime(now)
                .updateBy(event.getUserId())
                .updateTime(now)
                .build();
    }

    /**
     * 登录事件上下文（参数对象）
     */
    @Data
    @Builder
    private static class LoginEvent {
        private Long userId;
        private String username;
        private String loginIdentity;
        private String loginIp;
        private String deviceType;
        private String userAgent;
        private int status;
        private String message;
        private LocalDateTime loginTime;
    }
}
