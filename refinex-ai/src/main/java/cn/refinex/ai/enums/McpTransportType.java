package cn.refinex.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MCP 传输类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum McpTransportType {

    STDIO("stdio", "本地进程 stdio"),
    SSE("sse", "Server-Sent Events"),
    WS("ws", "WebSocket"),
    HTTP("http", "HTTP/HTTPS");

    /**
     * 值
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据值获取枚举
     *
     * @param code 值
     * @return 枚举
     */
    public static McpTransportType fromCode(String code) {
        for (McpTransportType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown McpTransportType: " + code);
    }
}

