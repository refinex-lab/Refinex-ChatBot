package cn.refinex.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户性别
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum UserSex {

    MALE("MALE", "男"),
    FEMALE("FEMALE", "女"),
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
     * 所有编码
     */
    public static final Set<String> ALL_CODES = Stream.of(values())
            .map(UserSex::getCode)
            .collect(Collectors.toSet());

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 用户性别枚举
     */
    public static UserSex fromCode(String code) {
        for (UserSex sex : values()) {
            if (sex.code.equals(code)) {
                return sex;
            }
        }
        throw new IllegalArgumentException("未知的用户性别: " + code);
    }
}
