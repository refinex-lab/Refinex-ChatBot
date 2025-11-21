package cn.refinex.ai.controller.mcp.dto.request;

import cn.refinex.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MCP Server 分页请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "MCP Server 分页请求")
public class AiMcpServerPageRequest extends PageQuery {

    @Schema(description = "传输类型")
    private String transportType;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "关键字")
    private String keyword;
}
