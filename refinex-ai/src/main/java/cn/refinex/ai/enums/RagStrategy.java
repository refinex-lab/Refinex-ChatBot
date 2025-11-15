package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * RAG 检索策略
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum RagStrategy {

    VECTOR("VECTOR", "向量检索"),
    BM25("BM25", "BM25 文本检索"),
    HYBRID("HYBRID", "混合检索"),
    MMR("MMR", "最大边际相关"),
    RRF("RRF", "反向排名融合");

    /**
     * 策略代码
     */
    private final String code;

    /**
     * 策略描述
     */
    private final String description;

    /**
     * 根据代码获取策略
     *
     * @param code 策略代码
     * @return 策略
     */
    public static RagStrategy fromCode(String code) {
        for (RagStrategy t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown RagStrategy: " + code);
    }
}

