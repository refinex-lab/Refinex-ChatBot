package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对话角色
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MessageRole {

    SYSTEM("system", "系统"),
    USER("user", "用户"),
    ASSISTANT("assistant", "助手"),
    TOOL("tool", "工具");

    /**
     * 角色代码
     */
    private final String code;

    /**
     * 角色描述
     */
    private final String description;

    /**
     * 根据代码获取角色
     *
     * @param code 代码
     * @return 角色
     */
    public static MessageRole fromCode(String code) {
        for (MessageRole t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown MessageRole: " + code);
    }
}

