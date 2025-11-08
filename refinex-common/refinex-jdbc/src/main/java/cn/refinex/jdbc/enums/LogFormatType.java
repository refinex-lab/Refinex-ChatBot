package cn.refinex.jdbc.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志格式类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LogFormatType {

    /**
     * 文本格式
     */
    TEXT("text"),

    /**
     * JSON格式
     */
    JSON("json"),

    ;

    /**
     * 日志格式类型值
     */
    private final String value;
}
