package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 鉴权类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum AuthType {

    NONE("NONE", "无鉴权"),
    BEARER("BEARER", "Bearer 令牌"),
    BASIC("BASIC", "Basic 认证");

    /**
     * 鉴权类型代码
     */
    private final String code;

    /**
     * 鉴权类型描述
     */
    private final String description;

    /**
     * 根据代码获取鉴权类型
     *
     * @param code 代码
     * @return 鉴权类型
     */
    public static AuthType fromCode(String code) {
        for (AuthType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown AuthType: " + code);
    }
}

