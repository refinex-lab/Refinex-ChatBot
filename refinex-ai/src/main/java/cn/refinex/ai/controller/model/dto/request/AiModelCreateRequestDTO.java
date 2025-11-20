package cn.refinex.ai.controller.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 创建模型请求对象
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "创建AI模型请求")
public record AiModelCreateRequestDTO(

        @NotNull(message = "供应商ID不能为空")
        @Schema(description = "供应商ID")
        Long providerId,

        @NotBlank(message = "模型标识不能为空")
        @Size(max = 100, message = "模型标识长度不能超过100")
        @Schema(description = "供应商模型标识")
        String modelKey,

        @Size(max = 100, message = "模型名称长度不能超过100")
        @Schema(description = "模型名称")
        String modelName,

        @NotBlank(message = "模型类型不能为空")
        @Size(max = 30, message = "模型类型长度不能超过30")
        @Schema(description = "模型类型: CHAT/EMBEDDING 等")
        String modelType,

        @Size(max = 30, message = "API 形态长度不能超过30")
        @Schema(description = "API 形态: openai/vertex/azure 等")
        String apiVariant,

        @Size(max = 50, message = "区域长度不能超过50")
        @Schema(description = "区域(如 azure/bedrock region)")
        String region,

        @Schema(description = "上下文窗口大小")
        Integer contextWindowTokens,

        @Schema(description = "最大输出 tokens")
        Integer maxOutputTokens,

        @Schema(description = "输入/1K tokens 价格")
        BigDecimal priceInputPer1k,

        @Schema(description = "输出/1K tokens 价格")
        BigDecimal priceOutputPer1k,

        @Size(max = 16, message = "币种长度不能超过16")
        @Schema(description = "币种，默认USD")
        String currency,

        @Schema(description = "是否支持工具调用, 0/1")
        Integer supportToolCall,

        @Schema(description = "是否支持图像多模态, 0/1")
        Integer supportVision,

        @Schema(description = "是否支持音频输入, 0/1")
        Integer supportAudioIn,

        @Schema(description = "是否支持音频输出, 0/1")
        Integer supportAudioOut,

        @Schema(description = "是否支持结构化输出, 0/1")
        Integer supportStructuredOut,

        @Schema(description = "状态:1启用,0停用, 默认1")
        Integer status,

        @Size(max = 500, message = "备注长度不能超过500")
        @Schema(description = "备注")
        String remark
) {
}
