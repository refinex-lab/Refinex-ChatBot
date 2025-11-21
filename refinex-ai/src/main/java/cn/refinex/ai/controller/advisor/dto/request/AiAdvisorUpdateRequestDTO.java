package cn.refinex.ai.controller.advisor.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Advisor 更新请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "Advisor 更新请求")
public record AiAdvisorUpdateRequestDTO(

        @NotBlank(message = "Advisor 编码不能为空")
        @Size(max = 100, message = "Advisor 编码长度不能超过100个字符")
        @Schema(description = "Advisor 编码")
        String advisorCode,

        @NotBlank(message = "Advisor 名称不能为空")
        @Size(max = 100, message = "Advisor 名称长度不能超过100个字符")
        @Schema(description = "Advisor 名称")
        String advisorName,

        @NotBlank(message = "Advisor 类型不能为空")
        @Size(max = 50, message = "Advisor 类型长度不能超过50个字符")
        @Schema(description = "Advisor 类型")
        String advisorType,

        @Schema(description = "排序")
        Integer sort,

        @Schema(description = "状态")
        Integer status,

        @Schema(description = "备注")
        String remark
) {
}
