package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工具类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ToolType {

    FUNCTION("FUNCTION", "函数/本地实现"),
    HTTP("HTTP", "HTTP 请求"),
    MCP_TOOL("MCP_TOOL", "MCP 工具"),
    RAG_QUERY("RAG_QUERY", "RAG 检索"),
    SCRIPT("SCRIPT", "脚本"),
    SYSTEM("SYSTEM", "系统内置");

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
    public static ToolType fromCode(String code) {
        for (ToolType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown ToolType: " + code);
    }
}

