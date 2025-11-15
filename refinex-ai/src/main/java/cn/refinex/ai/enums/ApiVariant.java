package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 形态
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ApiVariant {

    OPENAI("openai", "OpenAI 兼容接口"),
    VERTEX("vertex", "Vertex AI"),
    BEDROCK("bedrock", "AWS Bedrock"),
    AZURE("azure", "Azure OpenAI"),
    OLLAMA("ollama", "Ollama 本地");

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
    public static ApiVariant fromCode(String code) {
        for (ApiVariant t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown ApiVariant: " + code);
    }
}

