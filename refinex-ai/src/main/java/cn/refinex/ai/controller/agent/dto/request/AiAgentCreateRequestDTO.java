package cn.refinex.ai.controller.agent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Agent 创建请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "Agent 创建请求")
public record AiAgentCreateRequestDTO(

        @NotBlank(message = "Agent 编码不能为空")
        @Size(max = 100, message = "Agent 编码长度不能超过 100 个字符")
        @Schema(description = "Agent 编码，唯一")
        String agentCode,

        @NotBlank(message = "Agent 名称不能为空")
        @Size(max = 100, message = "Agent 名称长度不能超过 100 个字符")
        @Schema(description = "Agent 名称")
        String agentName,

        @Schema(description = "说明")
        String description,

        @NotNull(message = "默认模型ID不能为空")
        @Schema(description = "默认模型ID")
        Long modelId,

        @Schema(description = "提示词ID")
        Long promptId,

        @Schema(description = "结构化输出 Schema ID")
        Long outputSchemaId,

        @Schema(description = "默认知识库ID")
        Long ragKbId,

        @Schema(description = "温度值")
        BigDecimal temperature,

        @Schema(description = "TopP")
        BigDecimal topP,

        @Schema(description = "Presence Penalty")
        BigDecimal presencePenalty,

        @Schema(description = "Frequency Penalty")
        BigDecimal frequencyPenalty,

        @Schema(description = "最大生成 tokens")
        Integer maxTokens,

        @Schema(description = "停止词")
        List<String> stopSequences,

        @Schema(description = "工具选择策略")
        String toolChoice,

        @Schema(description = "状态:1启用,0停用")
        Integer status,

        @Schema(description = "备注")
        String remark,

        @Schema(description = "工具ID列表，保持顺序")
        List<Long> toolIds,

        @Schema(description = "Advisor ID 列表，保持顺序")
        List<Long> advisorIds
) {
}
