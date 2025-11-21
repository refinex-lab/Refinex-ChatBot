package cn.refinex.ai.controller.agent.dto.request;

import cn.refinex.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Agent 分页请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Agent 分页请求")
public class AiAgentPageRequest extends PageQuery {

    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "供应商ID")
    private Long providerId;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "关键字(编码/名称)")
    private String keyword;
}
