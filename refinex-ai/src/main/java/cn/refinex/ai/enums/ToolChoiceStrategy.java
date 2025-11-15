package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工具选择策略
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ToolChoiceStrategy {

    AUTO("auto", "自动"),
    NONE("none", "不使用工具"),
    REQUIRED("required", "必须使用工具"),
    NAMED("named", "指定某个工具名称");

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
     * @param code 代码
     * @return 策略
     */
    public static ToolChoiceStrategy fromCode(String code) {
        for (ToolChoiceStrategy t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown ToolChoiceStrategy: " + code);
    }
}

