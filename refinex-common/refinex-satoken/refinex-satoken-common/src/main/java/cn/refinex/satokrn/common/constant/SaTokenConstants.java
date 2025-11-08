package cn.refinex.satokrn.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sa-Token 常量类
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SaTokenConstants {

    /**
     * Session 中存储 LoginUser 的 key
     */
    public static final String LOGIN_USER_KEY = "loginUser";

    /**
     * SAME-Token 请求头名称
     */
    public static final String SAME_TOKEN_HEADER = "Same-Token";

    /**
     * 默认 Token 名称
     */
    public static final String DEFAULT_TOKEN_NAME = "Authorization";

    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 管理员角色编码
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * 普通用户角色编码
     */
    public static final String ROLE_USER = "ROLE_USER";
}
