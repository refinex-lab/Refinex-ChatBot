package cn.refinex.ai.controller.agent.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 响应
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Schema(description = "Agent 响应对象")
public class AiAgentResponseDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "Agent 编码")
    private String agentCode;

    @Schema(description = "Agent 名称")
    private String agentName;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "默认模型ID")
    private Long modelId;

    @Schema(description = "提示词ID")
    private Long promptId;

    @Schema(description = "结构化输出 Schema ID")
    private Long outputSchemaId;

    @Schema(description = "默认知识库ID")
    private Long ragKbId;

    @Schema(description = "温度值")
    private BigDecimal temperature;

    @Schema(description = "TopP")
    private BigDecimal topP;

    @Schema(description = "Presence Penalty")
    private BigDecimal presencePenalty;

    @Schema(description = "Frequency Penalty")
    private BigDecimal frequencyPenalty;

    @Schema(description = "最大生成 tokens")
    private Integer maxTokens;

    @Schema(description = "停止词")
    private List<String> stopSequences;

    @Schema(description = "工具选择策略")
    private String toolChoice;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "工具ID列表")
    private List<Long> toolIds;

    @Schema(description = "Advisor ID 列表")
    private List<Long> advisorIds;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
