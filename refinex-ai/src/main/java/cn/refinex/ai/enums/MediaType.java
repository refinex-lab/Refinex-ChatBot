package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 媒体类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MediaType {

    IMAGE("IMAGE", "图片"),
    AUDIO("AUDIO", "音频"),
    VIDEO("VIDEO", "视频"),
    DOCUMENT("DOCUMENT", "文档"),
    OTHER("OTHER", "其他");

    /**
     * 媒体类型代码
     */
    private final String code;

    /**
     * 媒体类型描述
     */
    private final String description;

    /**
     * 根据代码获取媒体类型
     *
     * @param code 媒体类型代码
     * @return 媒体类型
     */
    public static MediaType fromCode(String code) {
        for (MediaType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown MediaType: " + code);
    }
}

