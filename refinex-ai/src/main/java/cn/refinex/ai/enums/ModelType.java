package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模型类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ModelType {

    CHAT("CHAT", "对话"),
    EMBEDDING("EMBEDDING", "向量"),
    IMAGE("IMAGE", "图像"),
    AUDIO("AUDIO", "音频"),
    RERANK("RERANK", "重排"),
    MCP("MCP", "模型上下文协议");

    /**
     * 模型类型代码
     */
    private final String code;

    /**
     * 模型类型描述
     */
    private final String description;

    /**
     * 根据代码获取模型类型
     *
     * @param code 模型类型代码
     * @return 模型类型
     */
    public static ModelType fromCode(String code) {
        for (ModelType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown ModelType: " + code);
    }
}

