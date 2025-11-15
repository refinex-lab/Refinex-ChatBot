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
 * AI 模型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI 模型")
public class AiModel extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "供应商ID(逻辑关联 ai_provider.id)")
    private Long providerId;

    @Schema(description = "供应商模型标识: 如 gpt-4o-mini,text-embedding-3-large")
    private String modelKey;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型类型: CHAT/EMBEDDING/IMAGE/AUDIO/RERANK/MCP")
    private String modelType;

    @Schema(description = "API 形态: openai/vertex/bedrock/azure/ollama 等")
    private String apiVariant;

    @Schema(description = "区域(如 azure/bedrock 的 region)")
    private String region;

    @Schema(description = "上下文窗口大小")
    private Integer contextWindowTokens;

    @Schema(description = "最大输出 tokens")
    private Integer maxOutputTokens;

    @Schema(description = "输入/1K tokens 价格")
    private BigDecimal priceInputPer1k;

    @Schema(description = "输出/1K tokens 价格")
    private BigDecimal priceOutputPer1k;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "是否支持工具调用")
    private Integer supportToolCall;

    @Schema(description = "是否支持图像多模态")
    private Integer supportVision;

    @Schema(description = "是否支持音频输入")
    private Integer supportAudioIn;

    @Schema(description = "是否支持音频输出")
    private Integer supportAudioOut;

    @Schema(description = "是否支持结构化输出")
    private Integer supportStructuredOut;

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

