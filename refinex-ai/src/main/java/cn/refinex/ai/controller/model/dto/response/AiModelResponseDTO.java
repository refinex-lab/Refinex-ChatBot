package cn.refinex.ai.controller.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型响应对象
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "AI 模型响应")
public record AiModelResponseDTO(

        @Schema(description = "主键ID")
        Long id,

        @Schema(description = "供应商ID")
        Long providerId,

        @Schema(description = "模型标识")
        String modelKey,

        @Schema(description = "模型名称")
        String modelName,

        @Schema(description = "模型类型")
        String modelType,

        @Schema(description = "API 形态")
        String apiVariant,

        @Schema(description = "区域")
        String region,

        @Schema(description = "上下文窗口大小")
        Integer contextWindowTokens,

        @Schema(description = "最大输出 tokens")
        Integer maxOutputTokens,

        @Schema(description = "输入价格/1K tokens")
        BigDecimal priceInputPer1k,

        @Schema(description = "输出价格/1K tokens")
        BigDecimal priceOutputPer1k,

        @Schema(description = "币种")
        String currency,

        @Schema(description = "是否支持工具调用")
        Integer supportToolCall,

        @Schema(description = "是否支持图像多模态")
        Integer supportVision,

        @Schema(description = "是否支持音频输入")
        Integer supportAudioIn,

        @Schema(description = "是否支持音频输出")
        Integer supportAudioOut,

        @Schema(description = "是否支持结构化输出")
        Integer supportStructuredOut,

        @Schema(description = "状态:1启用,0停用")
        Integer status,

        @Schema(description = "备注")
        String remark,

        @Schema(description = "创建人")
        Long createBy,

        @Schema(description = "创建时间")
        LocalDateTime createTime,

        @Schema(description = "更新人")
        Long updateBy,

        @Schema(description = "更新时间")
        LocalDateTime updateTime,

        @Schema(description = "删除人")
        Long deleteBy,

        @Schema(description = "删除时间")
        LocalDateTime deleteTime
) {
}
