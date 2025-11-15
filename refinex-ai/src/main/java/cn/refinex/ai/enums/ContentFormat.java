package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 内容格式
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ContentFormat {

    TEXT("TEXT", "纯文本"),
    MARKDOWN("MARKDOWN", "Markdown"),
    JSON("JSON", "JSON");

    /**
     * 枚举值
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据枚举值获取枚举对象
     *
     * @param code 枚举值
     * @return 枚举对象
     */
    public static ContentFormat fromCode(String code) {
        for (ContentFormat t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown ContentFormat: " + code);
    }
}

