package cn.refinex.ai.controller.tool.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工具响应对象
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Schema(description = "工具响应对象")
public class AiToolResponseDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "工具编码")
    private String toolCode;

    @Schema(description = "工具名称")
    private String toolName;

    @Schema(description = "工具类型")
    private String toolType;

    @Schema(description = "实现 Bean 名")
    private String implBean;

    @Schema(description = "端点")
    private String endpoint;

    @Schema(description = "超时时间")
    private Integer timeoutMs;

    @Schema(description = "输入 Schema(JSON)")
    private Map<String, Object> inputSchema;

    @Schema(description = "输出 Schema(JSON)")
    private Map<String, Object> outputSchema;

    @Schema(description = "MCP Server ID")
    private Long mcpServerId;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
