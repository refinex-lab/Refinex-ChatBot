package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天消息类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ChatMessageType {

    NORMAL("NORMAL", "普通消息"),
    ERROR("ERROR", "错误消息"),
    TOOL_CALL("TOOL_CALL", "工具调用"),
    TOOL_RESULT("TOOL_RESULT", "工具结果"),
    EVENT("EVENT", "事件/系统消息");

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
    public static ChatMessageType fromCode(String code) {
        for (ChatMessageType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown ChatMessageType: " + code);
    }
}

