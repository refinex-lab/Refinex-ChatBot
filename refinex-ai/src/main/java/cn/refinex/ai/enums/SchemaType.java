package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Schema 类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum SchemaType {

    JSON_SCHEMA("JSON_SCHEMA", "JSON-Schema"),
    PROTO("PROTO", "Protocol Buffers"),
    XML("XML", "XML"),
    YAML("YAML", "YAML");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据代码获取类型
     *
     * @param code 类型代码
     * @return 类型
     */
    public static SchemaType fromCode(String code) {
        for (SchemaType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown SchemaType: " + code);
    }
}

