package cn.refinex.platform.listener;

import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.refinex.core.util.StringUtils;
import cn.refinex.platform.constants.AuthConstants;
import cn.refinex.platform.service.LoginAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Sa-Token 登录事件监听器
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginEventListener extends SaTokenListenerForSimple {

    private final LoginAuditService loginAuditService;

    /**
     * 登录成功时调用, 记录登录成功日志
     *
     * @param loginType  登录类型
     * @param loginId    登录 ID
     * @param tokenValue 令牌值
     * @param parameter  登录参数
     */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter parameter) {
        if (Objects.isNull(parameter)) {
            log.debug("Sa-Token doLogin 参数缺失，跳过登录日志记录");
            return;
        }

        Long userId = parseUserId(loginId);
        String loginIdentity = getExtra(parameter, AuthConstants.EXTRA_LOGIN_IDENTITY);
        String loginIp = getExtra(parameter, AuthConstants.EXTRA_LOGIN_IP);
        String deviceType = parameter.getDeviceType();
        if (StringUtils.isBlank(deviceType)) {
            deviceType = getExtra(parameter, AuthConstants.EXTRA_LOGIN_DEVICE);
        }
        String userAgent = getExtra(parameter, AuthConstants.EXTRA_LOGIN_USER_AGENT);
        String username = getExtra(parameter, AuthConstants.EXTRA_LOGIN_USERNAME);
        loginAuditService.recordLoginSuccess(userId, username, loginIdentity, loginIp, deviceType, userAgent);
    }

    /**
     * 从登录参数中获取额外信息
     *
     * @param parameter 登录参数
     * @param key       键
     * @return 值
     */
    private String getExtra(SaLoginParameter parameter, String key) {
        Object value = parameter.getExtra(key);
        return Objects.nonNull(value) ? value.toString() : null;
    }

    /**
     * 解析用户 ID
     *
     * @param loginId 登录 ID
     * @return 用户 ID
     */
    private Long parseUserId(Object loginId) {
        if (Objects.isNull(loginId)) {
            return null;
        }
        if (loginId instanceof Long l) {
            return l;
        }
        try {
            return Long.parseLong(loginId.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析 loginId: {}", loginId);
            return null;
        }
    }
}
