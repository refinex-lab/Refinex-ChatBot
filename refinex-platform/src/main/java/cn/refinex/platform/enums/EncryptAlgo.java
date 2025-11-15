package cn.refinex.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 加密算法
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum EncryptAlgo {

    NONE("NONE", "不加密"),
    AES256("AES256", "AES-256"),
    KMS("KMS", "KMS 托管密钥");

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
    public static EncryptAlgo fromCode(String code) {
        for (EncryptAlgo t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown EncryptAlgo: " + code);
    }
}

