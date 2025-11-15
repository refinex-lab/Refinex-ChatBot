package cn.refinex.kb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 可见性
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum Visibility {

    PRIVATE("PRIVATE", "仅自己可见"),
    TEAM("TEAM", "团队可见"),
    ORG("ORG", "组织可见"),
    PUBLIC("PUBLIC", "公开可见");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    public static Visibility fromCode(String code) {
        for (Visibility t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown Visibility: " + code);
    }
}

