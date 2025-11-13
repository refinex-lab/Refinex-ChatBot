package cn.refinex.satoken.common.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账号状态枚举
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AccountStatus {

    NORMAL(1, "正常"),
    FROZEN(2, "冻结"),
    CANCELLED(3, "注销");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String desc;

    /**
     * 根据状态码获取账号状态枚举
     *
     * @param code 状态码
     * @return 账号状态枚举
     */
    public static AccountStatus fromCode(Integer code) {
        for (AccountStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
