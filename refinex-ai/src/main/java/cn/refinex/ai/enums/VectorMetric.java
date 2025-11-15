package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 向量度量方式
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum VectorMetric {

    COSINE("cosine", "余弦相似度"),
    L2("l2", "欧氏距离"),
    IP("ip", "内积");

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
    public static VectorMetric fromCode(String code) {
        for (VectorMetric t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown VectorMetric: " + code);
    }
}

