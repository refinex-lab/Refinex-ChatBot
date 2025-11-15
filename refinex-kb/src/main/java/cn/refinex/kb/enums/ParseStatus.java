package cn.refinex.kb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档解析状态
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ParseStatus {

    PENDING("PENDING", "待解析"),
    PARSING("PARSING", "解析中"),
    DONE("DONE", "完成"),
    FAILED("FAILED", "失败");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据状态码获取状态
     *
     * @param code 状态码
     * @return 状态
     */
    public static ParseStatus fromCode(String code) {
        for (ParseStatus t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown ParseStatus: " + code);
    }
}

