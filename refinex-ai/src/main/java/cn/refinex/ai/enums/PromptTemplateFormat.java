package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提示词模板格式
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PromptTemplateFormat {

    SPRING("SPRING", "Spring AI 模板"),
    MUSTACHE("MUSTACHE", "Mustache 模板"),
    STRING("STRING", "字符串拼接"),
    LITERAL("LITERAL", "原文/固定文本");

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
    public static PromptTemplateFormat fromCode(String code) {
        for (PromptTemplateFormat t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown PromptTemplateFormat: " + code);
    }
}

