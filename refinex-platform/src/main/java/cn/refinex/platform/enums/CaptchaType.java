package cn.refinex.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码类型枚举
 *
 * @author Refinex
 * @since 2025-10-05
 */
@Getter
@AllArgsConstructor
public enum CaptchaType {

    /**
     * PNG 类型（线段干扰）
     */
    SPEC("spec", "PNG 类型"),

    /**
     * GIF 动画类型
     */
    GIF("gif", "GIF 动画类型"),

    /**
     * 算术验证码
     */
    ARITHMETIC("arithmetic", "算术验证码"),

    /**
     * 中文验证码
     */
    CHINESE("chinese", "中文验证码"),

    /**
     * 中文 GIF 验证码
     */
    CHINESE_GIF("chinese_gif", "中文 GIF 验证码");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 验证码类型枚举
     */
    public static CaptchaType fromCode(String code) {
        for (CaptchaType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的验证码类型: " + code);
    }
}
