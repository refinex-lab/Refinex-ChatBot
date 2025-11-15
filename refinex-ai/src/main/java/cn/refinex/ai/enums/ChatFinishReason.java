package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天结束原因
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ChatFinishReason {

    STOP("stop", "正常结束"),
    LENGTH("length", "达到长度限制"),
    TOOL_CALLS("tool_calls", "工具调用"),
    OTHER("other", "其他");

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
    public static ChatFinishReason fromCode(String code) {
        for (ChatFinishReason t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown ChatFinishReason: " + code);
    }
}

