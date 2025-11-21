package cn.refinex.ai.controller.tool.dto.request;

import cn.refinex.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工具分页请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工具分页请求")
public class AiToolPageRequest extends PageQuery {

    @Schema(description = "工具类型")
    private String toolType;

    @Schema(description = "MCP Server ID")
    private Long mcpServerId;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "关键字(编码/名称)")
    private String keyword;
}
