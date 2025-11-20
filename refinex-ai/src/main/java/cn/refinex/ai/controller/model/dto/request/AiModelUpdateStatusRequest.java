package cn.refinex.ai.controller.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 模型状态更新请求
 *
 * @author Refinex
 * @since 1.0.0
 */
public record AiModelUpdateStatusRequest(

        @Schema(description = "状态：1启用，0停用", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "状态不能为空")
        Integer status
) {
}
