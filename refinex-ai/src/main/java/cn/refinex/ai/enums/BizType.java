package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件业务类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum BizType {

    CHAT_MSG("CHAT_MSG", "聊天消息"),
    USER_AVATAR("USER_AVATAR", "用户头像"),
    DOC("DOC", "普通文档"),
    KB("KB", "知识库"),
    OTHER("OTHER", "其他");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    public static BizType fromCode(String code) {
        for (BizType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown BizType: " + code);
    }
}

