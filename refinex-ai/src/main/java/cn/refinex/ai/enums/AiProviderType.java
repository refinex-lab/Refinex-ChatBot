package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 供应商类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum AiProviderType {

    PUBLIC("public", "公共云/公共服务"),
    PRIVATE("private", "私有化/专有"),
    SELF_HOSTED("self_hosted", "自托管");

    /**
     * 值
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据值获取枚举
     *
     * @param code 值
     * @return 枚举
     */
    public static AiProviderType fromCode(String code) {
        for (AiProviderType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown AiProviderType: " + code);
    }
}

