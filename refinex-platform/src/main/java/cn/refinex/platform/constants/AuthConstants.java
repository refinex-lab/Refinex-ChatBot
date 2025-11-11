package cn.refinex.platform.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 认证相关常量
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthConstants {

    /**
     * 登录身份标识
     */
    public static final String EXTRA_LOGIN_IDENTITY = "loginIdentity";

    /**
     * 登录 IP 地址
     */
    public static final String EXTRA_LOGIN_IP = "loginIp";

    /**
     * 登录设备类型
     */
    public static final String EXTRA_LOGIN_DEVICE = "loginDevice";

    /**
     * 登录用户代理
     */
    public static final String EXTRA_LOGIN_USER_AGENT = "loginUserAgent";

    /**
     * 登录用户名
     */
    public static final String EXTRA_LOGIN_USERNAME = "loginUsername";
}
