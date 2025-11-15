package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 使用操作类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum UsageOperation {

    CHAT("CHAT", "对话"),
    EMBEDDING("EMBEDDING", "向量"),
    IMAGE("IMAGE", "图像"),
    AUDIO("AUDIO", "音频"),
    TOOL("TOOL", "工具调用"),
    RERANK("RERANK", "重排");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据代码获取类型
     *
     * @param code 类型代码
     * @return 类型
     */
    public static UsageOperation fromCode(String code) {
        for (UsageOperation t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown UsageOperation: " + code);
    }
}

