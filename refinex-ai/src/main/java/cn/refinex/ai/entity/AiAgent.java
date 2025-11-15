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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Agent/助手定义
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Agent/助手定义")
public class AiAgent extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "Agent 编码，唯一")
    private String agentCode;

    @Schema(description = "Agent 名称")
    private String agentName;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "默认模型ID(逻辑关联 ai_model.id)")
    private Long modelId;

    @Schema(description = "提示词ID(逻辑关联 ai_prompt.id)")
    private Long promptId;

    @Schema(description = "结构化输出 Schema(逻辑关联 ai_schema.id)")
    private Long outputSchemaId;

    @Schema(description = "默认知识库ID(逻辑关联 kb_base.id)")
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

    @Schema(description = "停止词(JSON)")
    private String stopSequences;

    @Schema(description = "工具选择策略: auto/none/required/指定tool")
    private String toolChoice;

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

