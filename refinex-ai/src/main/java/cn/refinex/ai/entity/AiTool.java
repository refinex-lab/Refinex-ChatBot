package cn.refinex.ai.entity;

import cn.refinex.jdbc.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 工具定义
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI 工具定义")
public class AiTool extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "工具编码，唯一")
    private String toolCode;

    @Schema(description = "工具名称")
    private String toolName;

    @Schema(description = "工具类型: FUNCTION/HTTP/MCP_TOOL/RAG_QUERY/SCRIPT/SYSTEM")
    private String toolType;

    @Schema(description = "实现 Bean 名或类名(用于 Spring 调用)")
    private String implBean;

    @Schema(description = "HTTP/Shell 等端点(可选)")
    private String endpoint;

    @Schema(description = "超时时间(ms)")
    private Integer timeoutMs;

    @Schema(description = "输入 JSON-Schema")
    private String inputSchema;

    @Schema(description = "输出 JSON-Schema")
    private String outputSchema;

    @Schema(description = "关联的 MCP Server(逻辑关联 ai_mcp_server.id)")
    private Long mcpServerId;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "删除人ID")
    private Long deleteBy;

    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

    @Schema(description = "备注")
    private String remark;
}

