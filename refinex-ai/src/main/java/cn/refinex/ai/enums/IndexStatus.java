package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档索引状态
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum IndexStatus {

    PENDING("PENDING", "待索引"),
    INDEXING("INDEXING", "索引中"),
    DONE("DONE", "完成"),
    FAILED("FAILED", "失败");

    /**
     * 索引状态代码
     */
    private final String code;

    /**
     * 索引状态描述
     */
    private final String description;

    /**
     * 根据代码获取索引状态
     *
     * @param code 代码
     * @return 索引状态
     */
    public static IndexStatus fromCode(String code) {
        for (IndexStatus t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown IndexStatus: " + code);
    }
}

