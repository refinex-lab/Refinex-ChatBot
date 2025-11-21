package cn.refinex.ai.controller.tool.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 工具状态更新请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "工具状态更新请求")
public record AiToolUpdateStatusRequest(

        @NotNull(message = "状态不能为空")
        @Schema(description = "状态:1启用,0停用")
        Integer status
) {
}
