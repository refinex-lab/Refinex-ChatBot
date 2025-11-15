package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Advisor 类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum AdvisorType {

    MODERATION("MODERATION", "内容审核"),
    RETRY("RETRY", "重试"),
    GUARDRAIL("GUARDRAIL", "护栏"),
    VALIDATOR("VALIDATOR", "校验"),
    ROUTER("ROUTER", "路由"),
    CONTEXT_ENRICH("CONTEXT_ENRICH", "上下文增强");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据代码获取 AdvisorType
     *
     * @param code 代码
     * @return AdvisorType
     */
    public static AdvisorType fromCode(String code) {
        for (AdvisorType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown AdvisorType: " + code);
    }
}

